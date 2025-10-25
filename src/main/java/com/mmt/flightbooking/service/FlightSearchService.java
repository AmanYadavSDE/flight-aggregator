package com.mmt.flightbooking.service;

import com.mmt.flightbooking.dto.FlightSearchRequest;
import com.mmt.flightbooking.dto.FlightSearchResult;
import com.mmt.flightbooking.dto.FlightSearchResponse;
import com.mmt.flightbooking.service.airline.ExternalAirlineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class FlightSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(FlightSearchService.class);
    
    @Autowired
    private ExternalAirlineService airlineService;
    
    @Autowired
    private FlightSearchCacheService searchCacheService;

    public FlightSearchResponse searchFlights(FlightSearchRequest request) {
        logger.info("Searching flights for route: {} to {} on {}", 
                   request.getOrigin(), request.getDestination(), request.getDepartureDate());
        
        try {
            // PURE AGGREGATOR APPROACH (like MMT):
            // Search ONLY from external airlines, NO local database
            
            // 1. Search external airlines asynchronously (parallel calls)
            CompletableFuture<List<FlightSearchResult>> externalResults = 
                airlineService.searchFlightsAsync(request);
            
            // 2. Get external results
            List<FlightSearchResult> allResults = externalResults.get();
            
            // 3. Cache each flight result for later booking (30 min TTL)
            logger.info("Caching {} flight results in Redis...", allResults.size());
            for (FlightSearchResult flight : allResults) {
                try {
                    searchCacheService.cacheFlightResult(flight.getFlightId(), flight);
                    logger.debug("Cached flight: {} - {}", flight.getFlightId(), flight.getFlightNumber());
                } catch (Exception e) {
                    logger.error("Failed to cache flight: {}", flight.getFlightId(), e);
                }
            }
            logger.info("Successfully cached {} flights", allResults.size());
            
            // 4. Apply filters and sorting
            List<FlightSearchResult> filteredResults = applyFiltersAndSorting(allResults, request);
            
            // 5. Create response
            FlightSearchResponse response = new FlightSearchResponse(filteredResults);
            response.setSearchId(UUID.randomUUID().toString());
            
            logger.info("Found {} flights for search", filteredResults.size());
            return response;
            
        } catch (Exception e) {
            logger.error("Error searching flights", e);
            FlightSearchResponse errorResponse = new FlightSearchResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Flight search failed: " + e.getMessage());
            return errorResponse;
        }
    }
    
    private List<FlightSearchResult> applyFiltersAndSorting(
            List<FlightSearchResult> results, FlightSearchRequest request) {
        
        // Apply seat class filter
        if (!"ECONOMY".equals(request.getSeatClass())) {
            // For now, all flights are economy class
            // In a real system, you'd filter by actual seat class
        }
        
        // Apply sorting
        String sortBy = request.getSortBy();
        String sortOrder = request.getSortOrder();
        
        if ("PRICE".equals(sortBy)) {
            results.sort((a, b) -> {
                int comparison = a.getPrice().compareTo(b.getPrice());
                return "DESC".equals(sortOrder) ? -comparison : comparison;
            });
        } else if ("DURATION".equals(sortBy)) {
            results.sort((a, b) -> {
                int comparison = a.getDurationMinutes().compareTo(b.getDurationMinutes());
                return "DESC".equals(sortOrder) ? -comparison : comparison;
            });
        } else if ("DEPARTURE_TIME".equals(sortBy)) {
            results.sort((a, b) -> {
                int comparison = a.getDepartureTime().compareTo(b.getDepartureTime());
                return "DESC".equals(sortOrder) ? -comparison : comparison;
            });
        }
        
        return results;
    }
}
