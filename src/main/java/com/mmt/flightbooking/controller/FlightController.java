package com.mmt.flightbooking.controller;

import com.mmt.flightbooking.dto.FlightSearchRequest;
import com.mmt.flightbooking.dto.FlightSearchResponse;
import com.mmt.flightbooking.service.FlightSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/flights")
@Tag(name = "Flight Search", description = "Flight search and discovery APIs")
public class FlightController {
    
    private static final Logger logger = LoggerFactory.getLogger(FlightController.class);
    
    @Autowired
    private FlightSearchService flightSearchService;
    
    @GetMapping("/search")
    @Operation(summary = "Search flights", description = "Search for flights between two airports on a specific date")
    public ResponseEntity<FlightSearchResponse> searchFlights(@Valid FlightSearchRequest request) {
        logger.info("Flight search request: {} to {} on {}", 
                   request.getOrigin(), request.getDestination(), request.getDepartureDate());
        
        try {
            FlightSearchResponse response = flightSearchService.searchFlights(request);
            
            if (response.isSuccess()) {
                logger.info("Flight search completed successfully. Found {} flights", 
                           response.getTotalCount());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Flight search failed: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Flight search error", e);
            FlightSearchResponse errorResponse = new FlightSearchResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Flight search failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if flight search service is healthy")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Flight search service is running");
    }
}
