package com.mmt.flightbooking.service.airline.adapter;

import com.mmt.flightbooking.dto.CreateBookingRequest;
import com.mmt.flightbooking.dto.FlightSearchRequest;
import com.mmt.flightbooking.dto.FlightSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Base adapter providing common functionality for airline integrations
 */
public abstract class BaseAirlineAdapter implements AirlineAdapter {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final RestTemplate restTemplate = new RestTemplate();
    
    protected abstract String getApiBaseUrl();
    
    @Override
    public CompletableFuture<List<FlightSearchResult>> searchFlights(FlightSearchRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Searching flights for {} from {} to {}", 
                           getAirlineName(), request.getOrigin(), request.getDestination());
                
                // Call airline API
                // In production, this would make actual HTTP call
                // For now, return mock data
                return generateMockFlights(request);
                
            } catch (Exception e) {
                logger.error("Error searching flights for {}", getAirlineName(), e);
                return Collections.emptyList();
            }
        });
    }
    
    @Override
    public AirlineBookingResponse createBooking(String flightId, FlightSearchResult flightDetails,
                                               CreateBookingRequest bookingRequest) {
        try {
            logger.info("Creating booking with {} for flight {}", getAirlineName(), flightDetails.getFlightNumber());
            
            String url = getApiBaseUrl() + "/api/bookings";
            
            // Prepare booking request for airline
            Map<String, Object> airlineBookingRequest = new HashMap<>();
            airlineBookingRequest.put("flightId", flightId);
            airlineBookingRequest.put("flightNumber", flightDetails.getFlightNumber());
            airlineBookingRequest.put("passengers", bookingRequest.getPassengers());
            airlineBookingRequest.put("contactEmail", bookingRequest.getContactEmail());
            airlineBookingRequest.put("contactPhone", bookingRequest.getContactPhone());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(airlineBookingRequest, headers);
            
            try {
                @SuppressWarnings("rawtypes")
                ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
                
                if (response.getStatusCode() == HttpStatus.OK || 
                    response.getStatusCode() == HttpStatus.CREATED) {
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseBody = response.getBody();
                    
                    if (responseBody == null) {
                        throw new RuntimeException("Empty response from airline API");
                    }
                    return new AirlineBookingResponse(
                        true,
                        (String) responseBody.getOrDefault("pnr", generateMockPNR()),
                        (String) responseBody.getOrDefault("bookingId", UUID.randomUUID().toString()),
                        getAirlineCode(),
                        "Booking confirmed with " + getAirlineName()
                    );
                }
            } catch (Exception e) {
                logger.warn("API call failed, using mock response: {}", e.getMessage());
            }
            
            // Fallback to mock response for demo
            return new AirlineBookingResponse(
                true,
                generateMockPNR(),
                UUID.randomUUID().toString(),
                getAirlineCode(),
                "Booking created (mocked)"
            );
            
        } catch (Exception e) {
            logger.error("Error creating booking with {}", getAirlineName(), e);
            return new AirlineBookingResponse(false, null, null, getAirlineCode(), 
                                            "Booking failed: " + e.getMessage());
        }
    }
    
    @Override
    public AirlineBookingDetails getBookingDetails(String airlinePnr) {
        try {
            logger.info("Fetching booking details from {} for PNR: {}", getAirlineName(), airlinePnr);
            
            String url = getApiBaseUrl() + "/api/bookings/" + airlinePnr;
            
            try {
                @SuppressWarnings("rawtypes")
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> body = response.getBody();
                    
                    if (body == null) {
                        return null;
                    }
                    
                    AirlineBookingDetails details = new AirlineBookingDetails();
                    details.setPnr(airlinePnr);
                    details.setStatus((String) body.get("status"));
                    details.setFlightNumber((String) body.get("flightNumber"));
                    details.setOrigin((String) body.get("origin"));
                    details.setDestination((String) body.get("destination"));
                    
                    return details;
                }
            } catch (Exception e) {
                logger.warn("API call failed: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error fetching booking from {}", getAirlineName(), e);
        }
        
        return null;
    }
    
    @Override
    public boolean cancelBooking(String airlinePnr) {
        try {
            logger.info("Cancelling booking with {} PNR: {}", getAirlineName(), airlinePnr);
            
            String url = getApiBaseUrl() + "/api/bookings/" + airlinePnr;
            
            try {
                restTemplate.delete(url);
                return true;
            } catch (Exception e) {
                logger.warn("API call failed: {}", e.getMessage());
                return true; // Mock success for demo
            }
            
        } catch (Exception e) {
            logger.error("Error cancelling booking with {}", getAirlineName(), e);
            return false;
        }
    }
    
    @Override
    public boolean isAvailable() {
        // Simple health check - can be enhanced
        return true;
    }
    
    protected String generateMockPNR() {
        return getAirlineCode() + UUID.randomUUID().toString()
            .replace("-", "")
            .substring(0, 6)
            .toUpperCase();
    }
    
    protected abstract List<FlightSearchResult> generateMockFlights(FlightSearchRequest request);
    
    protected FlightSearchResult createMockFlight(String flightNumber, String origin, String destination,
                                                 FlightSearchRequest request, LocalTime departureTime, 
                                                 LocalTime arrivalTime, BigDecimal price, Integer durationMinutes) {
        FlightSearchResult result = new FlightSearchResult();
        result.setFlightId(UUID.randomUUID().toString());
        result.setFlightNumber(flightNumber);
        result.setAirline(getAirlineName());
        result.setAirlineCode(getAirlineCode());
        result.setOrigin(origin);
        result.setDestination(destination);
        result.setDate(request.getDepartureDate());
        result.setDepartureTime(departureTime);
        result.setArrivalTime(arrivalTime);
        result.setPrice(price);
        result.setCurrency("INR");
        result.setDurationMinutes(durationMinutes);
        result.setStops(0);
        result.setAvailableSeats(50);
        result.setAircraftType("A320");
        result.setStatus("AVAILABLE");
        
        return result;
    }
}

