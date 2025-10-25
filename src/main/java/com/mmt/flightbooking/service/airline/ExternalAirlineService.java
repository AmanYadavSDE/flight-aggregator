package com.mmt.flightbooking.service.airline;

import com.mmt.flightbooking.dto.FlightSearchRequest;
import com.mmt.flightbooking.dto.FlightSearchResult;
import com.mmt.flightbooking.service.airline.adapter.AirlineAdapter;
import com.mmt.flightbooking.service.airline.adapter.AirlineAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * External Airline Service using Adapter Pattern
 * Searches flights from all available airline adapters in parallel
 */
@Service
public class ExternalAirlineService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalAirlineService.class);
    
    @Autowired
    private AirlineAdapterFactory adapterFactory;
    
    /**
     * Search flights from all available airlines asynchronously
     * Uses Adapter Pattern to query all airline adapters in parallel
     * @param request Flight search criteria
     * @return Future containing combined results from all airlines
     */
    @Async
    public CompletableFuture<List<FlightSearchResult>> searchFlightsAsync(FlightSearchRequest request) {
        logger.info("Searching external airlines for route: {} to {}", 
                   request.getOrigin(), request.getDestination());
        
        try {
            // Get all available airline adapters
            List<AirlineAdapter> adapters = adapterFactory.getAvailableAdapters();
            logger.info("Searching {} airlines in parallel", adapters.size());
            
            // Create futures for all airline adapters
            List<CompletableFuture<List<FlightSearchResult>>> futures = adapters.stream()
                .map(adapter -> {
                    logger.debug("Initiating search with airline: {}", adapter.getAirlineName());
                    return adapter.searchFlights(request);
                })
                .collect(Collectors.toList());
            
            // Wait for all results and combine
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<FlightSearchResult> allResults = futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
                    
                    logger.info("Combined results from all airlines: {} flights found", allResults.size());
                    return allResults;
                });
                    
        } catch (Exception e) {
            logger.error("Error in external airline search", e);
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }
}
