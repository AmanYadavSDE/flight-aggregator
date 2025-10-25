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
 * Adapter for Vistara Airlines API
 */
@Component
public class VistaraAdapter extends BaseAirlineAdapter {
    
    @Value("${external.airlines.vistara.url:http://mock-airline-api:8080}")
    private String apiBaseUrl;
    
    @Override
    public String getAirlineCode() {
        return "UK";
    }
    
    @Override
    public String getAirlineName() {
        return "Vistara";
    }
    
    @Override
    protected String getApiBaseUrl() {
        return apiBaseUrl;
    }
    
    @Override
    protected List<FlightSearchResult> generateMockFlights(FlightSearchRequest request) {
        List<FlightSearchResult> results = new ArrayList<>();
        
        // Vistara evening flight
        results.add(createMockFlight(
            "UK-567",
            request.getOrigin(),
            request.getDestination(),
            request,
            LocalTime.of(16, 40),
            LocalTime.of(18, 55),
            new BigDecimal("6800"),
            135
        ));
        
        logger.info("Generated {} Vistara flights", results.size());
        return results;
    }
}

