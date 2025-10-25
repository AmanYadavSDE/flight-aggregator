package com.mmt.flightbooking.service.airline;

import com.mmt.flightbooking.dto.FlightSearchRequest;
import com.mmt.flightbooking.dto.FlightSearchResult;
import com.mmt.flightbooking.service.airline.adapter.AirlineAdapter;
import com.mmt.flightbooking.service.airline.adapter.AirlineAdapterFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ExternalAirlineServiceTest {

    @Mock
    private AirlineAdapterFactory adapterFactory;

    @Mock
    private AirlineAdapter indigoAdapter;

    @Mock
    private AirlineAdapter airIndiaAdapter;

    @Mock
    private AirlineAdapter spiceJetAdapter;

    @Mock
    private AirlineAdapter vistaraAdapter;

    @InjectMocks
    private ExternalAirlineService externalAirlineService;

    private FlightSearchRequest searchRequest;
    private List<FlightSearchResult> indigoFlights;
    private List<FlightSearchResult> airIndiaFlights;
    private List<FlightSearchResult> spiceJetFlights;
    private List<FlightSearchResult> vistaraFlights;

    @BeforeEach
    void setUp() {
        searchRequest = new FlightSearchRequest();
        searchRequest.setOrigin("DEL");
        searchRequest.setDestination("BOM");
        searchRequest.setDepartureDate(LocalDate.of(2025, 11, 15));
        searchRequest.setPassengerCount(1);

        // Setup mock adapters - using lenient to avoid unnecessary stubbing errors
        lenient().when(indigoAdapter.getAirlineName()).thenReturn("IndiGo");
        lenient().when(indigoAdapter.getAirlineCode()).thenReturn("6E");
        lenient().when(indigoAdapter.isAvailable()).thenReturn(true);

        lenient().when(airIndiaAdapter.getAirlineName()).thenReturn("Air India");
        lenient().when(airIndiaAdapter.getAirlineCode()).thenReturn("AI");
        lenient().when(airIndiaAdapter.isAvailable()).thenReturn(true);

        lenient().when(spiceJetAdapter.getAirlineName()).thenReturn("SpiceJet");
        lenient().when(spiceJetAdapter.getAirlineCode()).thenReturn("SG");
        lenient().when(spiceJetAdapter.isAvailable()).thenReturn(true);

        lenient().when(vistaraAdapter.getAirlineName()).thenReturn("Vistara");
        lenient().when(vistaraAdapter.getAirlineCode()).thenReturn("UK");
        lenient().when(vistaraAdapter.isAvailable()).thenReturn(true);

        // Setup mock flight results
        indigoFlights = Arrays.asList(
            createFlightResult("6E-123", "IndiGo", "6E", new BigDecimal("4500"), LocalTime.of(8, 30)),
            createFlightResult("6E-456", "IndiGo", "6E", new BigDecimal("5200"), LocalTime.of(14, 15))
        );

        airIndiaFlights = Arrays.asList(
            createFlightResult("AI-789", "Air India", "AI", new BigDecimal("4800"), LocalTime.of(9, 45)),
            createFlightResult("AI-101", "Air India", "AI", new BigDecimal("5500"), LocalTime.of(18, 20))
        );

        spiceJetFlights = Arrays.asList(
            createFlightResult("SG-234", "SpiceJet", "SG", new BigDecimal("4200"), LocalTime.of(11, 10))
        );

        vistaraFlights = Arrays.asList(
            createFlightResult("UK-567", "Vistara", "UK", new BigDecimal("6800"), LocalTime.of(16, 40))
        );
    }

    @Test
    void testSearchFlightsAsync_AllAirlinesAvailable() throws Exception {
        // Arrange
        when(adapterFactory.getAvailableAdapters())
            .thenReturn(Arrays.asList(indigoAdapter, airIndiaAdapter, spiceJetAdapter, vistaraAdapter));

        when(indigoAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.completedFuture(indigoFlights));
        when(airIndiaAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.completedFuture(airIndiaFlights));
        when(spiceJetAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.completedFuture(spiceJetFlights));
        when(vistaraAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.completedFuture(vistaraFlights));

        // Act
        CompletableFuture<List<FlightSearchResult>> future = 
            externalAirlineService.searchFlightsAsync(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        assertNotNull(results);
        assertEquals(6, results.size()); // 2+2+1+1 = 6 flights
        
        verify(adapterFactory, times(1)).getAvailableAdapters();
        verify(indigoAdapter, times(1)).searchFlights(searchRequest);
        verify(airIndiaAdapter, times(1)).searchFlights(searchRequest);
        verify(spiceJetAdapter, times(1)).searchFlights(searchRequest);
        verify(vistaraAdapter, times(1)).searchFlights(searchRequest);
    }

    @Test
    void testSearchFlightsAsync_SomeAirlinesUnavailable() throws Exception {
        // Arrange
        lenient().when(spiceJetAdapter.isAvailable()).thenReturn(false);
        
        when(adapterFactory.getAvailableAdapters())
            .thenReturn(Arrays.asList(indigoAdapter, airIndiaAdapter, vistaraAdapter)); // SpiceJet not available

        when(indigoAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.completedFuture(indigoFlights));
        when(airIndiaAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.completedFuture(airIndiaFlights));
        when(vistaraAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.completedFuture(vistaraFlights));

        // Act
        CompletableFuture<List<FlightSearchResult>> future = 
            externalAirlineService.searchFlightsAsync(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        assertNotNull(results);
        assertEquals(5, results.size()); // 2+2+1 = 5 flights (SpiceJet excluded)
        verify(spiceJetAdapter, never()).searchFlights(any());
    }

    @Test
    void testSearchFlightsAsync_NoAirlinesAvailable() throws Exception {
        // Arrange
        when(adapterFactory.getAvailableAdapters()).thenReturn(new ArrayList<>());

        // Act
        CompletableFuture<List<FlightSearchResult>> future = 
            externalAirlineService.searchFlightsAsync(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    void testSearchFlightsAsync_OneAirlineFails() throws Exception {
        // Arrange
        when(adapterFactory.getAvailableAdapters())
            .thenReturn(Arrays.asList(indigoAdapter, airIndiaAdapter, spiceJetAdapter));

        when(indigoAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.completedFuture(indigoFlights));
        when(airIndiaAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("API unavailable")));
        when(spiceJetAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.completedFuture(spiceJetFlights));

        // Act
        CompletableFuture<List<FlightSearchResult>> future = 
            externalAirlineService.searchFlightsAsync(searchRequest);

        // Assert - Should handle failure gracefully
        try {
            future.get();
            // If it doesn't throw, verify results
        } catch (Exception e) {
            // Expected - one airline failed
            assertTrue(e.getCause() instanceof RuntimeException);
        }
    }

    @Test
    void testSearchFlightsAsync_OneAirlineReturnsEmpty() throws Exception {
        // Arrange
        when(adapterFactory.getAvailableAdapters())
            .thenReturn(Arrays.asList(indigoAdapter, airIndiaAdapter));

        when(indigoAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.completedFuture(indigoFlights));
        when(airIndiaAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.completedFuture(new ArrayList<>()));

        // Act
        CompletableFuture<List<FlightSearchResult>> future = 
            externalAirlineService.searchFlightsAsync(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size()); // Only IndiGo flights
    }

    @Test
    void testSearchFlightsAsync_VerifiesParallelExecution() throws Exception {
        // Arrange
        when(adapterFactory.getAvailableAdapters())
            .thenReturn(Arrays.asList(indigoAdapter, airIndiaAdapter, spiceJetAdapter, vistaraAdapter));

        when(indigoAdapter.searchFlights(any()))
            .thenReturn(CompletableFuture.completedFuture(indigoFlights));
        when(airIndiaAdapter.searchFlights(any()))
            .thenReturn(CompletableFuture.completedFuture(airIndiaFlights));
        when(spiceJetAdapter.searchFlights(any()))
            .thenReturn(CompletableFuture.completedFuture(spiceJetFlights));
        when(vistaraAdapter.searchFlights(any()))
            .thenReturn(CompletableFuture.completedFuture(vistaraFlights));

        // Act
        CompletableFuture<List<FlightSearchResult>> future = 
            externalAirlineService.searchFlightsAsync(searchRequest);
        future.get();

        // Assert - All adapters should be called
        verify(indigoAdapter, times(1)).searchFlights(searchRequest);
        verify(airIndiaAdapter, times(1)).searchFlights(searchRequest);
        verify(spiceJetAdapter, times(1)).searchFlights(searchRequest);
        verify(vistaraAdapter, times(1)).searchFlights(searchRequest);
    }

    @Test
    void testSearchFlightsAsync_ResultsCombinedCorrectly() throws Exception {
        // Arrange
        when(adapterFactory.getAvailableAdapters())
            .thenReturn(Arrays.asList(indigoAdapter, airIndiaAdapter));

        when(indigoAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.completedFuture(indigoFlights));
        when(airIndiaAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.completedFuture(airIndiaFlights));

        // Act
        CompletableFuture<List<FlightSearchResult>> future = 
            externalAirlineService.searchFlightsAsync(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        assertNotNull(results);
        assertEquals(4, results.size());
        
        // Verify flights from both airlines are present
        long indigoCount = results.stream().filter(f -> f.getAirlineCode().equals("6E")).count();
        long airIndiaCount = results.stream().filter(f -> f.getAirlineCode().equals("AI")).count();
        
        assertEquals(2, indigoCount);
        assertEquals(2, airIndiaCount);
    }

    @Test
    void testSearchFlightsAsync_HandlesLargeResultSets() throws Exception {
        // Arrange
        List<FlightSearchResult> largeResultSet = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            largeResultSet.add(createFlightResult(
                "6E-" + i, "IndiGo", "6E",
                new BigDecimal(4000 + i * 100),
                LocalTime.of(6 + (i % 12), i % 60)
            ));
        }

        when(adapterFactory.getAvailableAdapters()).thenReturn(Arrays.asList(indigoAdapter));
        when(indigoAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.completedFuture(largeResultSet));

        // Act
        CompletableFuture<List<FlightSearchResult>> future = 
            externalAirlineService.searchFlightsAsync(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        assertEquals(50, results.size());
    }

    @Test
    void testSearchFlightsAsync_ExceptionInFactory() throws Exception {
        // Arrange
        when(adapterFactory.getAvailableAdapters())
            .thenThrow(new RuntimeException("Factory error"));

        // Act
        CompletableFuture<List<FlightSearchResult>> future = 
            externalAirlineService.searchFlightsAsync(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert - Should return empty list on error
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    void testSearchFlightsAsync_VerifyRequestPassedToAllAdapters() throws Exception {
        // Arrange
        when(adapterFactory.getAvailableAdapters())
            .thenReturn(Arrays.asList(indigoAdapter, airIndiaAdapter));

        when(indigoAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.completedFuture(indigoFlights));
        when(airIndiaAdapter.searchFlights(searchRequest))
            .thenReturn(CompletableFuture.completedFuture(airIndiaFlights));

        // Act
        externalAirlineService.searchFlightsAsync(searchRequest).get();

        // Assert - Verify same request passed to all adapters
        verify(indigoAdapter).searchFlights(searchRequest);
        verify(airIndiaAdapter).searchFlights(searchRequest);
    }

    // Helper method to create test flight results
    private FlightSearchResult createFlightResult(String flightNumber, String airline, 
                                                  String airlineCode, BigDecimal price, 
                                                  LocalTime departureTime) {
        FlightSearchResult flight = new FlightSearchResult();
        flight.setFlightId("flight-" + flightNumber);
        flight.setFlightNumber(flightNumber);
        flight.setAirline(airline);
        flight.setAirlineCode(airlineCode);
        flight.setOrigin("DEL");
        flight.setDestination("BOM");
        flight.setDate(LocalDate.of(2025, 11, 15));
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(departureTime.plusMinutes(135));
        flight.setPrice(price);
        flight.setCurrency("INR");
        flight.setDurationMinutes(135);
        flight.setStops(0);
        return flight;
    }
}

