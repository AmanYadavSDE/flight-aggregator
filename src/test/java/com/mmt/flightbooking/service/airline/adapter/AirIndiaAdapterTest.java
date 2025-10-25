package com.mmt.flightbooking.service.airline.adapter;

import com.mmt.flightbooking.dto.CreateBookingRequest;
import com.mmt.flightbooking.dto.FlightSearchRequest;
import com.mmt.flightbooking.dto.FlightSearchResult;
import com.mmt.flightbooking.dto.PassengerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AirIndiaAdapterTest {

    private AirIndiaAdapter airIndiaAdapter;
    private FlightSearchRequest searchRequest;
    private CreateBookingRequest bookingRequest;
    private FlightSearchResult flightDetails;

    @BeforeEach
    void setUp() {
        airIndiaAdapter = new AirIndiaAdapter();
        ReflectionTestUtils.setField(airIndiaAdapter, "apiBaseUrl", "http://mock-airindia-api:8080");

        // Setup search request
        searchRequest = new FlightSearchRequest();
        searchRequest.setOrigin("DEL");
        searchRequest.setDestination("BOM");
        searchRequest.setDepartureDate(LocalDate.of(2025, 11, 15));
        searchRequest.setPassengerCount(1);

        // Setup flight details
        flightDetails = new FlightSearchResult();
        flightDetails.setFlightId("flight-456");
        flightDetails.setFlightNumber("AI-101");
        flightDetails.setAirline("Air India");
        flightDetails.setAirlineCode("AI");
        flightDetails.setOrigin("DEL");
        flightDetails.setDestination("BOM");
        flightDetails.setDate(LocalDate.of(2025, 11, 15));
        flightDetails.setDepartureTime(LocalTime.of(18, 20));
        flightDetails.setPrice(new BigDecimal("5500.00"));

        // Setup booking request
        PassengerRequest passenger = new PassengerRequest();
        passenger.setFirstName("John");
        passenger.setLastName("Doe");
        passenger.setAge(30);
        passenger.setType("ADULT");

        bookingRequest = new CreateBookingRequest();
        bookingRequest.setFlightIds(Arrays.asList("flight-456"));
        bookingRequest.setPassengerCount(1);
        bookingRequest.setPassengers(Arrays.asList(passenger));
        bookingRequest.setContactEmail("test@example.com");
        bookingRequest.setContactPhone("+919876543210");
    }

    @Test
    void testGetAirlineCode() {
        // Act
        String airlineCode = airIndiaAdapter.getAirlineCode();

        // Assert
        assertEquals("AI", airlineCode);
    }

    @Test
    void testGetAirlineName() {
        // Act
        String airlineName = airIndiaAdapter.getAirlineName();

        // Assert
        assertEquals("Air India", airlineName);
    }

    @Test
    void testIsAvailable() {
        // Act
        boolean available = airIndiaAdapter.isAvailable();

        // Assert
        assertTrue(available);
    }

    @Test
    void testSearchFlights_Success() throws Exception {
        // Act
        CompletableFuture<List<FlightSearchResult>> future = airIndiaAdapter.searchFlights(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size()); // Air India generates 2 flights

        // Verify first flight (morning)
        FlightSearchResult flight1 = results.get(0);
        assertEquals("AI-789", flight1.getFlightNumber());
        assertEquals("Air India", flight1.getAirline());
        assertEquals("AI", flight1.getAirlineCode());
        assertEquals("DEL", flight1.getOrigin());
        assertEquals("BOM", flight1.getDestination());
        assertEquals(LocalTime.of(9, 45), flight1.getDepartureTime());
        assertEquals(LocalTime.of(12, 0), flight1.getArrivalTime());
        assertEquals(new BigDecimal("4800"), flight1.getPrice());

        // Verify second flight (evening)
        FlightSearchResult flight2 = results.get(1);
        assertEquals("AI-101", flight2.getFlightNumber());
        assertEquals(LocalTime.of(18, 20), flight2.getDepartureTime());
        assertEquals(new BigDecimal("5500"), flight2.getPrice());
    }

    @Test
    void testSearchFlights_VerifyAllFields() throws Exception {
        // Act
        CompletableFuture<List<FlightSearchResult>> future = airIndiaAdapter.searchFlights(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        for (FlightSearchResult flight : results) {
            assertNotNull(flight.getFlightId());
            assertTrue(flight.getFlightNumber().startsWith("AI-"));
            assertEquals("Air India", flight.getAirline());
            assertEquals("AI", flight.getAirlineCode());
            assertEquals("DEL", flight.getOrigin());
            assertEquals("BOM", flight.getDestination());
            assertEquals(LocalDate.of(2025, 11, 15), flight.getDate());
            assertNotNull(flight.getDepartureTime());
            assertNotNull(flight.getArrivalTime());
            assertNotNull(flight.getPrice());
            assertEquals("INR", flight.getCurrency());
            assertEquals(135, flight.getDurationMinutes());
            assertEquals(0, flight.getStops());
            assertEquals(50, flight.getAvailableSeats());
            assertEquals("A320", flight.getAircraftType());
            assertEquals("AVAILABLE", flight.getStatus());
        }
    }

    @Test
    void testCreateBooking_Success() {
        // Act
        AirlineBookingResponse response = airIndiaAdapter.createBooking(
            "flight-456", flightDetails, bookingRequest
        );

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getPnr());
        assertTrue(response.getPnr().startsWith("AI"));
        assertNotNull(response.getBookingId());
        assertEquals("AI", response.getAirlineCode());
        assertNotNull(response.getMessage());
    }

    @Test
    void testCreateBooking_GeneratesUniquePNRs() {
        // Act
        AirlineBookingResponse response1 = airIndiaAdapter.createBooking("f1", flightDetails, bookingRequest);
        AirlineBookingResponse response2 = airIndiaAdapter.createBooking("f2", flightDetails, bookingRequest);
        AirlineBookingResponse response3 = airIndiaAdapter.createBooking("f3", flightDetails, bookingRequest);

        // Assert
        assertNotEquals(response1.getPnr(), response2.getPnr());
        assertNotEquals(response2.getPnr(), response3.getPnr());
        assertNotEquals(response1.getPnr(), response3.getPnr());
    }

    @Test
    void testPNRFormat() {
        // Act
        AirlineBookingResponse response = airIndiaAdapter.createBooking(
            "flight-456", flightDetails, bookingRequest
        );

        // Assert - PNR should be AI + 6 alphanumeric characters
        assertEquals(8, response.getPnr().length());
        assertTrue(response.getPnr().startsWith("AI"));
        assertTrue(response.getPnr().substring(2).matches("[A-Z0-9]{6}"));
    }

    @Test
    void testGetBookingDetails() {
        // Act
        AirlineBookingDetails details = airIndiaAdapter.getBookingDetails("AI-ABC123");

        // Assert - Mock API returns null
        assertNull(details);
    }

    @Test
    void testCancelBooking() {
        // Act
        boolean result = airIndiaAdapter.cancelBooking("AI-ABC123");

        // Assert - Returns true as mock fallback
        assertTrue(result);
    }

    @Test
    void testSearchFlights_AsyncExecution() {
        // Act
        CompletableFuture<List<FlightSearchResult>> future = airIndiaAdapter.searchFlights(searchRequest);

        // Assert - Future should not be null and not completed immediately in async context
        assertNotNull(future);
    }

    @Test
    void testSearchFlights_DifferentOriginDestination() throws Exception {
        // Arrange
        searchRequest.setOrigin("BLR");
        searchRequest.setDestination("HYD");

        // Act
        CompletableFuture<List<FlightSearchResult>> future = airIndiaAdapter.searchFlights(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        assertNotNull(results);
        for (FlightSearchResult flight : results) {
            assertEquals("BLR", flight.getOrigin());
            assertEquals("HYD", flight.getDestination());
        }
    }
}

