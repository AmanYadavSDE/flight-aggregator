package com.mmt.flightbooking.service.airline;

import com.mmt.flightbooking.dto.FlightSearchRequest;
import com.mmt.flightbooking.dto.FlightSearchResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public interface AirlineAPIClient {
    CompletableFuture<List<FlightSearchResult>> searchFlights(FlightSearchRequest request);
}
