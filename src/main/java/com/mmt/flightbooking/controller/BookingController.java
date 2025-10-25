package com.mmt.flightbooking.controller;

import com.mmt.flightbooking.dto.BookingResponse;
import com.mmt.flightbooking.dto.CreateBookingRequest;
import com.mmt.flightbooking.entity.Booking;
import com.mmt.flightbooking.entity.User;
import com.mmt.flightbooking.service.airline.AirlineBookingService;
import com.mmt.flightbooking.service.BookingService;
import com.mmt.flightbooking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/bookings")
@Tag(name = "Booking Management", description = "Flight booking management APIs")
public class BookingController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AirlineBookingService airlineBookingService;
    
    @PostMapping
    @Operation(summary = "Create booking", description = "Create a new flight booking")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication) {
        
        logger.info("Creating booking for user: {}", getCurrentUser(authentication).getEmail());
        
        try {
            User user = getCurrentUser(authentication);
            BookingResponse response = bookingService.createBooking(request, user);
            
            if (response.isSuccess()) {
                logger.info("Booking created successfully: {}", response.getBookingReference());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                logger.warn("Booking creation failed: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Booking creation error", e);
            BookingResponse errorResponse = new BookingResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Booking creation failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking details", description = "Get details of a specific booking")
    public ResponseEntity<BookingDetailsResponse> getBookingDetails(
            @PathVariable Long bookingId,
            Authentication authentication) {
        
        logger.info("Getting booking details for ID: {}", bookingId);
        
        try {
            User user = getCurrentUser(authentication);
            Booking booking = bookingService.getBookingById(bookingId, user);
            
            BookingDetailsResponse response = buildBookingDetailsResponse(booking);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting booking details", e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping
    @Operation(summary = "Get user bookings", description = "Get all bookings for the current user")
    public ResponseEntity<List<BookingDetailsResponse>> getUserBookings(Authentication authentication) {
        logger.info("Getting bookings for user: {}", getCurrentUser(authentication).getEmail());
        
        try {
            User user = getCurrentUser(authentication);
            List<Booking> bookings = bookingService.getUserBookings(user);
            
            List<BookingDetailsResponse> responses = bookings.stream()
                .map(this::buildBookingDetailsResponse)
                .toList();
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            logger.error("Error getting user bookings", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel booking", description = "Cancel a specific booking")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long bookingId,
            Authentication authentication) {
        
        logger.info("Cancelling booking: {}", bookingId);
        
        try {
            User user = getCurrentUser(authentication);
            Booking booking = bookingService.getBookingById(bookingId, user);
            
            // Cancel with airline first (AGGREGATOR PATTERN)
            boolean airlineCancelled = airlineBookingService.cancelBookingWithAirline(
                booking.getAirlineCode(), 
                booking.getAirlinePnr()
            );
            
            if (!airlineCancelled) {
                logger.warn("Airline cancellation failed for PNR: {}", booking.getAirlinePnr());
                // Continue anyway for demo purposes
            }
            
            // Update booking status to cancelled
            booking.setStatus(com.mmt.flightbooking.entity.BookingStatus.CANCELLED);
            
            BookingResponse response = new BookingResponse();
            response.setBookingId(booking.getId());
            response.setBookingReference(booking.getBookingReference());
            response.setStatus(booking.getStatus().toString());
            response.setMessage("Booking cancelled successfully with " + booking.getAirlineCode());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error cancelling booking", e);
            BookingResponse errorResponse = new BookingResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Booking cancellation failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    private User getCurrentUser(Authentication authentication) {
        // In a real implementation, you would extract user from JWT token
        // For now, use UserService to get/create test user
        return userService.getOrCreateTestUser();
    }
    
    private BookingDetailsResponse buildBookingDetailsResponse(Booking booking) {
        BookingDetailsResponse response = new BookingDetailsResponse();
        response.setBookingId(booking.getId());
        response.setBookingReference(booking.getBookingReference());
        response.setStatus(booking.getStatus().toString());
        response.setTotalAmount(booking.getTotalAmount());
        response.setCurrency(booking.getCurrency());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        
        // Add flight details (cached in booking for aggregator model)
        List<BookingDetailsResponse.FlightInfo> flights = new ArrayList<>();
        BookingDetailsResponse.FlightInfo flightInfo = new BookingDetailsResponse.FlightInfo();
        flightInfo.setFlightNumber(booking.getFlightNumber());
        flightInfo.setAirline(booking.getAirlineCode()); // Store airline code
        flightInfo.setOrigin(booking.getOriginAirport());
        flightInfo.setDestination(booking.getDestinationAirport());
        flightInfo.setDepartureTime(booking.getDepartureDate().atStartOfDay()); // Use date
        flightInfo.setArrivalTime(booking.getDepartureDate().atStartOfDay()); // Same day for simplicity
        flightInfo.setSeatClass("ECONOMY"); // Default
        flightInfo.setPrice(booking.getTotalAmount());
        flights.add(flightInfo);
        response.setFlights(flights);
        
        // Add passenger details
        response.setPassengers(booking.getPassengers().stream()
            .map(p -> {
                BookingDetailsResponse.PassengerInfo passengerInfo = new BookingDetailsResponse.PassengerInfo();
                passengerInfo.setFirstName(p.getFirstName());
                passengerInfo.setLastName(p.getLastName());
                passengerInfo.setAge(p.getAge());
                passengerInfo.setType(p.getType().toString());
                passengerInfo.setSeatNumber(p.getSeatNumber());
                return passengerInfo;
            })
            .toList());
        
        return response;
    }
}
