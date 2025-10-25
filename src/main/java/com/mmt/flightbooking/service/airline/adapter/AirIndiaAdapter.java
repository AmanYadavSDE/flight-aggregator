package com.mmt.flightbooking.service.airline.adapter;

import com.mmt.flightbooking.dto.FlightSearchRequest;
import com.mmt.flightbooking.dto.FlightSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for Air India Airlines API
 */
@Component
public class AirIndiaAdapter extends BaseAirlineAdapter {
    
    @Value("${external.airlines.airindia.url:http://mock-airline-api:8080}")
    private String apiBaseUrl;
    
    @Override
    public String getAirlineCode() {
        return "AI";
    }
    
    @Override
    public String getAirlineName() {
        return "Air India";
    }
    
    @Override
    protected String getApiBaseUrl() {
        return apiBaseUrl;
    }
    
    @Override
    protected List<FlightSearchResult> generateMockFlights(FlightSearchRequest request) {
        List<FlightSearchResult> results = new ArrayList<>();
        
        // Air India morning flight
        results.add(createMockFlight(
            "AI-789",
            request.getOrigin(),
            request.getDestination(),
            request,
            LocalTime.of(9, 45),
            LocalTime.of(12, 0),
            new BigDecimal("4800"),
            135
        ));
        
        // Air India evening flight
        results.add(createMockFlight(
            "AI-101",
            request.getOrigin(),
            request.getDestination(),
            request,
            LocalTime.of(18, 20),
            LocalTime.of(20, 35),
            new BigDecimal("5500"),
            135
        ));
        
        logger.info("Generated {} Air India flights", results.size());
        return results;
    }
}

