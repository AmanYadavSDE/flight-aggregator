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
 * Adapter for SpiceJet Airlines API
 */
@Component
public class SpiceJetAdapter extends BaseAirlineAdapter {
    
    @Value("${external.airlines.spicejet.url:http://mock-airline-api:8080}")
    private String apiBaseUrl;
    
    @Override
    public String getAirlineCode() {
        return "SG";
    }
    
    @Override
    public String getAirlineName() {
        return "SpiceJet";
    }
    
    @Override
    protected String getApiBaseUrl() {
        return apiBaseUrl;
    }
    
    @Override
    protected List<FlightSearchResult> generateMockFlights(FlightSearchRequest request) {
        List<FlightSearchResult> results = new ArrayList<>();
        
        // SpiceJet mid-day flight
        results.add(createMockFlight(
            "SG-234",
            request.getOrigin(),
            request.getDestination(),
            request,
            LocalTime.of(11, 10),
            LocalTime.of(13, 25),
            new BigDecimal("4200"),
            135
        ));
        
        logger.info("Generated {} SpiceJet flights", results.size());
        return results;
    }
}

