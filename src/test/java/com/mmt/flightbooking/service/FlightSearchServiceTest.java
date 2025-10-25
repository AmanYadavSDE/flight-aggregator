package com.mmt.flightbooking.service;

import com.mmt.flightbooking.dto.FlightSearchRequest;
import com.mmt.flightbooking.dto.FlightSearchResponse;
import com.mmt.flightbooking.dto.FlightSearchResult;
import com.mmt.flightbooking.service.airline.ExternalAirlineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightSearchServiceTest {

    @Mock
    private ExternalAirlineService airlineService;

    @Mock
    private FlightSearchCacheService searchCacheService;

    @InjectMocks
    private FlightSearchService flightSearchService;

    private FlightSearchRequest searchRequest;
    private List<FlightSearchResult> mockFlightResults;

    @BeforeEach
    void setUp() {
        // Setup search request
        searchRequest = new FlightSearchRequest();
        searchRequest.setOrigin("DEL");
        searchRequest.setDestination("BOM");
        searchRequest.setDepartureDate(LocalDate.of(2025, 11, 15));
        searchRequest.setPassengerCount(1);
        searchRequest.setSeatClass("ECONOMY");
        searchRequest.setSortBy("PRICE");
        searchRequest.setSortOrder("ASC");

        // Setup mock flight results
        mockFlightResults = new ArrayList<>();
        mockFlightResults.add(createFlightResult("flight-1", "6E-2001", "IndiGo", "6E", 
            new BigDecimal("5500.00"), LocalTime.of(10, 30), LocalTime.of(12, 45), 135));
        mockFlightResults.add(createFlightResult("flight-2", "AI-102", "Air India", "AI", 
            new BigDecimal("7200.00"), LocalTime.of(14, 0), LocalTime.of(16, 30), 150));
        mockFlightResults.add(createFlightResult("flight-3", "SG-8156", "SpiceJet", "SG", 
            new BigDecimal("4800.00"), LocalTime.of(8, 15), LocalTime.of(10, 30), 135));
        mockFlightResults.add(createFlightResult("flight-4", "UK-941", "Vistara", "UK", 
            new BigDecimal("6500.00"), LocalTime.of(12, 0), LocalTime.of(14, 15), 135));
    }

    @Test
    void testSearchFlights_Success() {
        // Arrange
        CompletableFuture<List<FlightSearchResult>> future = 
            CompletableFuture.completedFuture(mockFlightResults);
        when(airlineService.searchFlightsAsync(searchRequest)).thenReturn(future);

        // Act
        FlightSearchResponse response = flightSearchService.searchFlights(searchRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getSearchId());
        assertEquals(4, response.getFlights().size());

        verify(airlineService, times(1)).searchFlightsAsync(searchRequest);
        verify(searchCacheService, times(4)).cacheFlightResult(anyString(), any(FlightSearchResult.class));
    }

    @Test
    void testSearchFlights_CachesAllResults() {
        // Arrange
        CompletableFuture<List<FlightSearchResult>> future = 
            CompletableFuture.completedFuture(mockFlightResults);
        when(airlineService.searchFlightsAsync(searchRequest)).thenReturn(future);

        ArgumentCaptor<String> flightIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<FlightSearchResult> flightCaptor = ArgumentCaptor.forClass(FlightSearchResult.class);

        // Act
        flightSearchService.searchFlights(searchRequest);

        // Assert
        verify(searchCacheService, times(4)).cacheFlightResult(
            flightIdCaptor.capture(), 
            flightCaptor.capture()
        );

        List<String> cachedFlightIds = flightIdCaptor.getAllValues();
        assertTrue(cachedFlightIds.contains("flight-1"));
        assertTrue(cachedFlightIds.contains("flight-2"));
        assertTrue(cachedFlightIds.contains("flight-3"));
        assertTrue(cachedFlightIds.contains("flight-4"));
    }

    @Test
    void testSearchFlights_SortByPrice_Ascending() {
        // Arrange
        searchRequest.setSortBy("PRICE");
        searchRequest.setSortOrder("ASC");

        CompletableFuture<List<FlightSearchResult>> future = 
            CompletableFuture.completedFuture(new ArrayList<>(mockFlightResults));
        when(airlineService.searchFlightsAsync(searchRequest)).thenReturn(future);

        // Act
        FlightSearchResponse response = flightSearchService.searchFlights(searchRequest);

        // Assert
        List<FlightSearchResult> flights = response.getFlights();
        assertEquals(new BigDecimal("4800.00"), flights.get(0).getPrice()); // SpiceJet
        assertEquals(new BigDecimal("5500.00"), flights.get(1).getPrice()); // IndiGo
        assertEquals(new BigDecimal("6500.00"), flights.get(2).getPrice()); // Vistara
        assertEquals(new BigDecimal("7200.00"), flights.get(3).getPrice()); // Air India
    }

    @Test
    void testSearchFlights_SortByPrice_Descending() {
        // Arrange
        searchRequest.setSortBy("PRICE");
        searchRequest.setSortOrder("DESC");

        CompletableFuture<List<FlightSearchResult>> future = 
            CompletableFuture.completedFuture(new ArrayList<>(mockFlightResults));
        when(airlineService.searchFlightsAsync(searchRequest)).thenReturn(future);

        // Act
        FlightSearchResponse response = flightSearchService.searchFlights(searchRequest);

        // Assert
        List<FlightSearchResult> flights = response.getFlights();
        assertEquals(new BigDecimal("7200.00"), flights.get(0).getPrice()); // Air India
        assertEquals(new BigDecimal("6500.00"), flights.get(1).getPrice()); // Vistara
        assertEquals(new BigDecimal("5500.00"), flights.get(2).getPrice()); // IndiGo
        assertEquals(new BigDecimal("4800.00"), flights.get(3).getPrice()); // SpiceJet
    }

    @Test
    void testSearchFlights_SortByDuration_Ascending() {
        // Arrange
        searchRequest.setSortBy("DURATION");
        searchRequest.setSortOrder("ASC");

        // Create flights with different durations
        List<FlightSearchResult> mixedDurationFlights = new ArrayList<>();
        mixedDurationFlights.add(createFlightWithDuration("f1", 180)); // 3 hours
        mixedDurationFlights.add(createFlightWithDuration("f2", 120)); // 2 hours
        mixedDurationFlights.add(createFlightWithDuration("f3", 150)); // 2.5 hours

        CompletableFuture<List<FlightSearchResult>> future = 
            CompletableFuture.completedFuture(mixedDurationFlights);
        when(airlineService.searchFlightsAsync(searchRequest)).thenReturn(future);

        // Act
        FlightSearchResponse response = flightSearchService.searchFlights(searchRequest);

        // Assert
        List<FlightSearchResult> flights = response.getFlights();
        assertEquals(120, flights.get(0).getDurationMinutes());
        assertEquals(150, flights.get(1).getDurationMinutes());
        assertEquals(180, flights.get(2).getDurationMinutes());
    }

    @Test
    void testSearchFlights_SortByDepartureTime_Ascending() {
        // Arrange
        searchRequest.setSortBy("DEPARTURE_TIME");
        searchRequest.setSortOrder("ASC");

        CompletableFuture<List<FlightSearchResult>> future = 
            CompletableFuture.completedFuture(new ArrayList<>(mockFlightResults));
        when(airlineService.searchFlightsAsync(searchRequest)).thenReturn(future);

        // Act
        FlightSearchResponse response = flightSearchService.searchFlights(searchRequest);

        // Assert
        List<FlightSearchResult> flights = response.getFlights();
        assertEquals(LocalTime.of(8, 15), flights.get(0).getDepartureTime());  // SpiceJet
        assertEquals(LocalTime.of(10, 30), flights.get(1).getDepartureTime()); // IndiGo
        assertEquals(LocalTime.of(12, 0), flights.get(2).getDepartureTime());  // Vistara
        assertEquals(LocalTime.of(14, 0), flights.get(3).getDepartureTime());  // Air India
    }

    @Test
    void testSearchFlights_NoResults() {
        // Arrange
        CompletableFuture<List<FlightSearchResult>> future = 
            CompletableFuture.completedFuture(new ArrayList<>());
        when(airlineService.searchFlightsAsync(searchRequest)).thenReturn(future);

        // Act
        FlightSearchResponse response = flightSearchService.searchFlights(searchRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(0, response.getFlights().size());
        verify(searchCacheService, never()).cacheFlightResult(anyString(), any());
    }

    @Test
    void testSearchFlights_ExceptionHandling() {
        // Arrange
        CompletableFuture<List<FlightSearchResult>> future = CompletableFuture.failedFuture(
            new RuntimeException("Airline API unavailable")
        );
        when(airlineService.searchFlightsAsync(searchRequest)).thenReturn(future);

        // Act
        FlightSearchResponse response = flightSearchService.searchFlights(searchRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Flight search failed"));
        verify(searchCacheService, never()).cacheFlightResult(anyString(), any());
    }

    @Test
    void testSearchFlights_CacheFailureDoesNotBreakSearch() {
        // Arrange
        CompletableFuture<List<FlightSearchResult>> future = 
            CompletableFuture.completedFuture(mockFlightResults);
        when(airlineService.searchFlightsAsync(searchRequest)).thenReturn(future);
        
        // Simulate cache failure for one specific flight only
        doNothing().when(searchCacheService).cacheFlightResult(eq("flight-1"), any());
        doThrow(new RuntimeException("Redis connection failed"))
            .when(searchCacheService).cacheFlightResult(eq("flight-2"), any());
        doNothing().when(searchCacheService).cacheFlightResult(eq("flight-3"), any());
        doNothing().when(searchCacheService).cacheFlightResult(eq("flight-4"), any());

        // Act
        FlightSearchResponse response = flightSearchService.searchFlights(searchRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(4, response.getFlights().size()); // Search still succeeds

        // Verify all flights were attempted to be cached
        verify(searchCacheService, times(4)).cacheFlightResult(anyString(), any());
    }

    @Test
    void testSearchFlights_GeneratesUniqueSearchId() {
        // Arrange
        CompletableFuture<List<FlightSearchResult>> future = 
            CompletableFuture.completedFuture(mockFlightResults);
        when(airlineService.searchFlightsAsync(any())).thenReturn(future);

        // Act
        FlightSearchResponse response1 = flightSearchService.searchFlights(searchRequest);
        FlightSearchResponse response2 = flightSearchService.searchFlights(searchRequest);

        // Assert
        assertNotNull(response1.getSearchId());
        assertNotNull(response2.getSearchId());
        assertNotEquals(response1.getSearchId(), response2.getSearchId());
    }

    @Test
    void testSearchFlights_DefaultSorting() {
        // Arrange - No sorting specified
        searchRequest.setSortBy(null);
        searchRequest.setSortOrder(null);

        CompletableFuture<List<FlightSearchResult>> future = 
            CompletableFuture.completedFuture(new ArrayList<>(mockFlightResults));
        when(airlineService.searchFlightsAsync(searchRequest)).thenReturn(future);

        // Act
        FlightSearchResponse response = flightSearchService.searchFlights(searchRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(4, response.getFlights().size());
    }

    @Test
    void testSearchFlights_VerifyRequestPassed() {
        // Arrange
        CompletableFuture<List<FlightSearchResult>> future = 
            CompletableFuture.completedFuture(mockFlightResults);
        
        ArgumentCaptor<FlightSearchRequest> requestCaptor = 
            ArgumentCaptor.forClass(FlightSearchRequest.class);
        when(airlineService.searchFlightsAsync(requestCaptor.capture())).thenReturn(future);

        // Act
        flightSearchService.searchFlights(searchRequest);

        // Assert
        FlightSearchRequest capturedRequest = requestCaptor.getValue();
        assertEquals("DEL", capturedRequest.getOrigin());
        assertEquals("BOM", capturedRequest.getDestination());
        assertEquals(LocalDate.of(2025, 11, 15), capturedRequest.getDepartureDate());
        assertEquals(1, capturedRequest.getPassengerCount());
        assertEquals("ECONOMY", capturedRequest.getSeatClass());
    }

    @Test
    void testSearchFlights_HandlesNullSortOrder() {
        // Arrange
        searchRequest.setSortBy("PRICE");
        searchRequest.setSortOrder(null); // Null sort order

        CompletableFuture<List<FlightSearchResult>> future = 
            CompletableFuture.completedFuture(new ArrayList<>(mockFlightResults));
        when(airlineService.searchFlightsAsync(searchRequest)).thenReturn(future);

        // Act
        FlightSearchResponse response = flightSearchService.searchFlights(searchRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        // Should default to ascending order
        List<FlightSearchResult> flights = response.getFlights();
        assertTrue(flights.get(0).getPrice().compareTo(flights.get(flights.size()-1).getPrice()) <= 0);
    }

    @Test
    void testSearchFlights_LargeResultSet() {
        // Arrange
        List<FlightSearchResult> largeResultSet = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeResultSet.add(createFlightResult(
                "flight-" + i, 
                "FL-" + i, 
                "Airline" + (i % 5), 
                "AL", 
                new BigDecimal(5000 + i * 100), 
                LocalTime.of(6 + (i % 16), i % 60),  // Changed to % 16 to avoid hour 24
                LocalTime.of(8 + (i % 14), i % 60),  // Changed to % 14 to avoid hour 24
                120 + (i % 180)
            ));
        }

        CompletableFuture<List<FlightSearchResult>> future = 
            CompletableFuture.completedFuture(largeResultSet);
        when(airlineService.searchFlightsAsync(searchRequest)).thenReturn(future);

        // Act
        FlightSearchResponse response = flightSearchService.searchFlights(searchRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(100, response.getFlights().size());
        verify(searchCacheService, times(100)).cacheFlightResult(anyString(), any());
    }

    @Test
    void testSearchFlights_DifferentSeatClasses() {
        // Arrange
        searchRequest.setSeatClass("BUSINESS");

        CompletableFuture<List<FlightSearchResult>> future = 
            CompletableFuture.completedFuture(mockFlightResults);
        when(airlineService.searchFlightsAsync(searchRequest)).thenReturn(future);

        // Act
        FlightSearchResponse response = flightSearchService.searchFlights(searchRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        // Current implementation doesn't filter, just passes to airline service
        assertEquals(4, response.getFlights().size());
    }

    // Helper methods
    private FlightSearchResult createFlightResult(String flightId, String flightNumber, 
                                                  String airline, String airlineCode,
                                                  BigDecimal price, LocalTime departureTime, 
                                                  LocalTime arrivalTime, Integer duration) {
        FlightSearchResult flight = new FlightSearchResult();
        flight.setFlightId(flightId);
        flight.setFlightNumber(flightNumber);
        flight.setAirline(airline);
        flight.setAirlineCode(airlineCode);
        flight.setOrigin("DEL");
        flight.setDestination("BOM");
        flight.setDate(LocalDate.of(2025, 11, 15));
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setPrice(price);
        flight.setCurrency("INR");
        flight.setDurationMinutes(duration);
        flight.setStops(0);
        flight.setAvailableSeats(50);
        flight.setStatus("AVAILABLE");
        return flight;
    }

    private FlightSearchResult createFlightWithDuration(String flightId, Integer durationMinutes) {
        FlightSearchResult flight = new FlightSearchResult();
        flight.setFlightId(flightId);
        flight.setFlightNumber("TEST-" + flightId);
        flight.setAirline("Test Airline");
        flight.setAirlineCode("TA");
        flight.setOrigin("DEL");
        flight.setDestination("BOM");
        flight.setDate(LocalDate.of(2025, 11, 15));
        flight.setDepartureTime(LocalTime.of(10, 0));
        flight.setArrivalTime(LocalTime.of(10, 0).plusMinutes(durationMinutes));
        flight.setPrice(new BigDecimal("5000.00"));
        flight.setCurrency("INR");
        flight.setDurationMinutes(durationMinutes);
        flight.setStops(0);
        return flight;
    }
}

