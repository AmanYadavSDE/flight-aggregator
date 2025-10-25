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
 * Adapter for IndiGo Airlines API
 */
@Component
public class IndiGoAdapter extends BaseAirlineAdapter {
    
    @Value("${external.airlines.indigo.url:http://mock-airline-api:8080}")
    private String apiBaseUrl;
    
    @Override
    public String getAirlineCode() {
        return "6E";
    }
    
    @Override
    public String getAirlineName() {
        return "IndiGo";
    }
    
    @Override
    protected String getApiBaseUrl() {
        return apiBaseUrl;
    }
    
    @Override
    protected List<FlightSearchResult> generateMockFlights(FlightSearchRequest request) {
        List<FlightSearchResult> results = new ArrayList<>();
        
        // IndiGo morning flight
        results.add(createMockFlight(
            "6E-123",
            request.getOrigin(),
            request.getDestination(),
            request,
            LocalTime.of(8, 30),
            LocalTime.of(10, 45),
            new BigDecimal("4500"),
            135
        ));
        
        // IndiGo afternoon flight
        results.add(createMockFlight(
            "6E-456",
            request.getOrigin(),
            request.getDestination(),
            request,
            LocalTime.of(14, 15),
            LocalTime.of(16, 30),
            new BigDecimal("5200"),
            135
        ));
        
        logger.info("Generated {} IndiGo flights", results.size());
        return results;
    }
}

