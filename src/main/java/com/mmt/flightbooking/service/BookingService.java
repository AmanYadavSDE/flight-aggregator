package com.mmt.flightbooking.service;

import com.mmt.flightbooking.dto.CreateBookingRequest;
import com.mmt.flightbooking.dto.BookingResponse;
import com.mmt.flightbooking.dto.PaymentRequest;
import com.mmt.flightbooking.dto.PaymentResult;
import com.mmt.flightbooking.dto.PassengerRequest;
import com.mmt.flightbooking.dto.FlightSearchResult;
import com.mmt.flightbooking.entity.*;
import com.mmt.flightbooking.repository.BookingRepository;
import com.mmt.flightbooking.service.airline.AirlineBookingService;
import com.mmt.flightbooking.service.airline.adapter.AirlineBookingResponse;
import com.mmt.flightbooking.service.notification.NotificationService;
import com.mmt.flightbooking.service.payment.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BookingService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private FlightSearchCacheService searchCacheService;
    
    @Autowired
    private AirlineBookingService airlineBookingService;
    
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, User user) {
        logger.info("Creating booking for user: {} with {} flights", 
                   user.getEmail(), request.getFlightIds().size());
        
        try {
            // ═══════════════════════════════════════════════════════════
            // TRUE AGGREGATOR FLOW (Like MMT) - Key Changes:
            // 1. NO database queries for flights
            // 2. Get flight details from search cache
            // 3. Call AIRLINE's booking API
            // 4. Store only REFERENCE to airline booking
            // ═══════════════════════════════════════════════════════════
            
            // 1. Get flight details from search results cache
            String flightId = request.getFlightIds().get(0); // Handling single flight for simplicity
            logger.info("[AGGREGATOR] Retrieving flight from cache: {}", flightId);
            FlightSearchResult flightDetails = searchCacheService.getFlightFromCache(flightId);
            
            if (flightDetails == null) {
                logger.error("[AGGREGATOR] Flight not found in cache: {}. User must search again.", flightId);
                throw new RuntimeException("Flight not found in search results. Please search again.");
            }
            logger.info("[AGGREGATOR] ✓ Flight found in cache: {} - {}", flightDetails.getFlightNumber(), flightDetails.getAirline());
            
            logger.info("[AGGREGATOR] Booking flight {} ({}) - Price: {} {}", 
                       flightDetails.getFlightNumber(), flightDetails.getAirline(),
                       flightDetails.getPrice(), flightDetails.getCurrency());
            
            // 2. Call AIRLINE's booking API (THIS IS WHAT MMT DOES!)
            logger.info("[AGGREGATOR] Calling {} airline API to create booking...", 
                       flightDetails.getAirline());
            
            AirlineBookingResponse airlineResponse = 
                airlineBookingService.createBookingWithAirline(flightId, flightDetails, request);
            
            if (!airlineResponse.isSuccess()) {
                throw new RuntimeException("Airline booking failed: " + airlineResponse.getMessage());
            }
            
            logger.info("[AGGREGATOR] ✓ Airline booking successful! Airline PNR: {}", 
                       airlineResponse.getPnr());
            
            // 3. Create booking in OUR database (storing ONLY the reference)
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setBookingReference(generateBookingReference()); // MMT's own reference
            booking.setStatus(BookingStatus.CONFIRMED);  // Airline confirmed it
            booking.setTotalAmount(flightDetails.getPrice());
            booking.setCurrency(flightDetails.getCurrency());
            
            // Store airline booking references (KEY AGGREGATOR PATTERN!)
            booking.setAirlineCode(flightDetails.getAirlineCode());
            booking.setAirlinePnr(airlineResponse.getPnr());  // ← Airline's PNR
            booking.setAirlineBookingId(airlineResponse.getBookingId());  // ← Airline's ID
            
            // Cache flight details for quick display (not the source of truth)
            booking.setFlightNumber(flightDetails.getFlightNumber());
            booking.setOriginAirport(flightDetails.getOrigin());
            booking.setDestinationAirport(flightDetails.getDestination());
            booking.setDepartureDate(flightDetails.getDate());
            
            // 4. Save MMT booking
            booking = bookingRepository.save(booking);
            
            // 5. Create passengers
            createPassengers(booking, request.getPassengers());
            
            // 6. Create payment record (we handle payment, not airline)
            Payment payment = paymentService.createPayment(booking, 
                PaymentMethod.valueOf(request.getPaymentMethod()));
            booking.setPayment(payment);
            
            // 7. Save updated booking
            booking = bookingRepository.save(booking);
            
            logger.info("[AGGREGATOR] ✓ MMT booking created: {} → Airline PNR: {}", 
                       booking.getBookingReference(), booking.getAirlinePnr());
            
            // 8. Create response
            BookingResponse response = new BookingResponse();
            response.setBookingId(booking.getId());
            response.setBookingReference(booking.getBookingReference());
            response.setStatus(booking.getStatus().toString());
            response.setTotalAmount(booking.getTotalAmount());
            response.setCurrency(booking.getCurrency());
            response.setCreatedAt(booking.getCreatedAt());
            response.setMessage(String.format("✓ Booking confirmed with %s (PNR: %s)", 
                               flightDetails.getAirline(), booking.getAirlinePnr()));
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error creating booking", e);
            BookingResponse errorResponse = new BookingResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Booking creation failed: " + e.getMessage());
            return errorResponse;
        }
    }
    
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BookingResponse confirmBooking(Long bookingId, PaymentRequest paymentRequest) {
        logger.info("Confirming booking: {}", bookingId);
        
        try {
            // Get booking with optimistic locking
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            // Process payment
            PaymentResult paymentResult = paymentService.processPayment(paymentRequest);
            
            if (paymentResult.isSuccess()) {
                // Update booking status
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
                
                // Send confirmation
                notificationService.sendBookingConfirmation(booking);
                
                BookingResponse response = new BookingResponse();
                response.setBookingId(booking.getId());
                response.setBookingReference(booking.getBookingReference());
                response.setStatus(booking.getStatus().toString());
                response.setTotalAmount(booking.getTotalAmount());
                response.setCurrency(booking.getCurrency());
                response.setCreatedAt(booking.getCreatedAt());
                
                logger.info("Booking confirmed successfully: {}", booking.getBookingReference());
                return response;
            } else {
                booking.setStatus(BookingStatus.FAILED);
                bookingRepository.save(booking);
                
                BookingResponse errorResponse = new BookingResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("Payment failed: " + paymentResult.getMessage());
                return errorResponse;
            }
            
        } catch (Exception e) {
            logger.error("Error confirming booking", e);
            BookingResponse errorResponse = new BookingResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Booking confirmation failed: " + e.getMessage());
            return errorResponse;
        }
    }
    
    public Booking getBookingById(Long bookingId, User user) {
        return bookingRepository.findById(bookingId)
            .filter(booking -> booking.getUser().equals(user))
            .orElseThrow(() -> new RuntimeException("Booking not found"));
    }
    
    public List<Booking> getUserBookings(User user) {
        return bookingRepository.findUserBookings(user);
    }
    
    private void createPassengers(Booking booking, List<PassengerRequest> passengerRequests) {
        for (PassengerRequest request : passengerRequests) {
            Passenger passenger = new Passenger();
            passenger.setBooking(booking);
            passenger.setFirstName(request.getFirstName());
            passenger.setLastName(request.getLastName());
            passenger.setAge(request.getAge());
            passenger.setType(PassengerType.valueOf(request.getType()));
            passenger.setPassportNumber(request.getPassportNumber());
            passenger.setNationality(request.getNationality());
            passenger.setSeatNumber(request.getSeatNumber());
            passenger.setMealPreference(request.getMealPreference());
            passenger.setSpecialRequests(request.getSpecialRequests());
            
            booking.getPassengers().add(passenger);
        }
    }
    
    private String generateBookingReference() {
        return "MMT" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
