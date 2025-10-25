package com.mmt.flightbooking.service.airline.adapter;

import com.mmt.flightbooking.dto.CreateBookingRequest;
import com.mmt.flightbooking.dto.FlightSearchRequest;
import com.mmt.flightbooking.dto.FlightSearchResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Adapter interface for external airline systems
 * Following Adapter Pattern to provide uniform interface for different airline APIs
 */
public interface AirlineAdapter {
    
    /**
     * Get airline code (e.g., "6E" for IndiGo, "AI" for Air India)
     */
    String getAirlineCode();
    
    /**
     * Get airline name (e.g., "IndiGo", "Air India")
     */
    String getAirlineName();
    
    /**
     * Search flights from this airline
     * @param request Flight search criteria
     * @return Future containing list of flight results
     */
    CompletableFuture<List<FlightSearchResult>> searchFlights(FlightSearchRequest request);
    
    /**
     * Create booking with airline
     * @param flightId Flight identifier
     * @param flightDetails Flight details from search
     * @param bookingRequest Booking request details
     * @return Booking response from airline
     */
    AirlineBookingResponse createBooking(String flightId, FlightSearchResult flightDetails, 
                                        CreateBookingRequest bookingRequest);
    
    /**
     * Get booking details from airline
     * @param airlinePnr Airline PNR/booking reference
     * @return Booking details
     */
    AirlineBookingDetails getBookingDetails(String airlinePnr);
    
    /**
     * Cancel booking with airline
     * @param airlinePnr Airline PNR/booking reference
     * @return true if cancellation successful
     */
    boolean cancelBooking(String airlinePnr);
    
    /**
     * Check if airline API is available
     * @return true if available, false otherwise
     */
    boolean isAvailable();
}

