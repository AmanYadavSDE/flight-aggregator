package com.mmt.flightbooking.service.airline;

import com.mmt.flightbooking.dto.CreateBookingRequest;
import com.mmt.flightbooking.dto.FlightSearchResult;
import com.mmt.flightbooking.service.airline.adapter.AirlineAdapter;
import com.mmt.flightbooking.service.airline.adapter.AirlineAdapterFactory;
import com.mmt.flightbooking.service.airline.adapter.AirlineBookingResponse;
import com.mmt.flightbooking.service.airline.adapter.AirlineBookingDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to handle booking creation with external airline systems
 * This is the key component that makes this a TRUE aggregator like MMT
 * Now uses Adapter Pattern for extensible airline integration
 */
@Service
public class AirlineBookingService {
    
    private static final Logger logger = LoggerFactory.getLogger(AirlineBookingService.class);
    
    @Autowired
    private AirlineAdapterFactory adapterFactory;
    
    /**
     * Create booking with the airline that owns the flight
     * This is what MMT actually does - delegates booking to the airline
     * Uses Adapter Pattern to handle different airline APIs
     */
    public AirlineBookingResponse createBookingWithAirline(
            String flightId, 
            FlightSearchResult flightDetails,
            CreateBookingRequest bookingRequest) {
        
        logger.info("Creating booking with airline: {} for flight: {}", 
                   flightDetails.getAirlineCode(), flightDetails.getFlightNumber());
        
        try {
            // Get appropriate airline adapter
            AirlineAdapter adapter = adapterFactory.getAdapter(flightDetails.getAirlineCode());
            
            // Delegate booking to airline adapter
            AirlineBookingResponse response = adapter.createBooking(flightId, flightDetails, bookingRequest);
            
            logger.info("Airline booking response: Success={}, PNR={}", 
                       response.isSuccess(), response.getPnr());
            
            return response;
            
        } catch (IllegalArgumentException e) {
            logger.error("Unsupported airline: {}", flightDetails.getAirlineCode(), e);
            
            AirlineBookingResponse errorResponse = new AirlineBookingResponse();
            errorResponse.setSuccess(false);
            errorResponse.setAirlineCode(flightDetails.getAirlineCode());
            errorResponse.setMessage("Unsupported airline: " + flightDetails.getAirlineCode());
            return errorResponse;
            
        } catch (Exception e) {
            logger.error("Error creating booking with airline: {}", flightDetails.getAirlineCode(), e);
            
            AirlineBookingResponse errorResponse = new AirlineBookingResponse();
            errorResponse.setSuccess(false);
            errorResponse.setAirlineCode(flightDetails.getAirlineCode());
            errorResponse.setMessage("Booking failed: " + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * Get booking details from airline system
     * MMT uses this to fetch latest booking status from airline
     * Uses Adapter Pattern to handle different airline APIs
     */
    public AirlineBookingDetails getBookingFromAirline(String airlineCode, String airlinePnr) {
        logger.info("Fetching booking from airline: {} with PNR: {}", airlineCode, airlinePnr);
        
        try {
            // Get appropriate airline adapter
            AirlineAdapter adapter = adapterFactory.getAdapter(airlineCode);
            
            // Delegate to airline adapter
            return adapter.getBookingDetails(airlinePnr);
            
        } catch (Exception e) {
            logger.error("Error fetching booking from airline", e);
            return null;
        }
    }
    
    /**
     * Cancel booking with airline
     * Uses Adapter Pattern to handle different airline APIs
     */
    public boolean cancelBookingWithAirline(String airlineCode, String airlinePnr) {
        logger.info("Cancelling booking with airline: {} PNR: {}", airlineCode, airlinePnr);
        
        try {
            // Get appropriate airline adapter
            AirlineAdapter adapter = adapterFactory.getAdapter(airlineCode);
            
            // Delegate to airline adapter
            return adapter.cancelBooking(airlinePnr);
            
        } catch (Exception e) {
            logger.error("Error cancelling booking with airline", e);
            return false;
        }
    }
}

