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
class IndiGoAdapterTest {

    private IndiGoAdapter indigoAdapter;
    private FlightSearchRequest searchRequest;
    private CreateBookingRequest bookingRequest;
    private FlightSearchResult flightDetails;

    @BeforeEach
    void setUp() {
        indigoAdapter = new IndiGoAdapter();
        ReflectionTestUtils.setField(indigoAdapter, "apiBaseUrl", "http://mock-indigo-api:8080");

        // Setup search request
        searchRequest = new FlightSearchRequest();
        searchRequest.setOrigin("DEL");
        searchRequest.setDestination("BOM");
        searchRequest.setDepartureDate(LocalDate.of(2025, 11, 15));
        searchRequest.setPassengerCount(1);

        // Setup flight details
        flightDetails = new FlightSearchResult();
        flightDetails.setFlightId("flight-123");
        flightDetails.setFlightNumber("6E-2001");
        flightDetails.setAirline("IndiGo");
        flightDetails.setAirlineCode("6E");
        flightDetails.setOrigin("DEL");
        flightDetails.setDestination("BOM");
        flightDetails.setDate(LocalDate.of(2025, 11, 15));
        flightDetails.setDepartureTime(LocalTime.of(10, 30));
        flightDetails.setPrice(new BigDecimal("5500.00"));

        // Setup booking request
        PassengerRequest passenger = new PassengerRequest();
        passenger.setFirstName("John");
        passenger.setLastName("Doe");
        passenger.setAge(30);
        passenger.setType("ADULT");

        bookingRequest = new CreateBookingRequest();
        bookingRequest.setFlightIds(Arrays.asList("flight-123"));
        bookingRequest.setPassengerCount(1);
        bookingRequest.setPassengers(Arrays.asList(passenger));
        bookingRequest.setContactEmail("test@example.com");
        bookingRequest.setContactPhone("+919876543210");
    }

    @Test
    void testGetAirlineCode() {
        // Act
        String airlineCode = indigoAdapter.getAirlineCode();

        // Assert
        assertEquals("6E", airlineCode);
    }

    @Test
    void testGetAirlineName() {
        // Act
        String airlineName = indigoAdapter.getAirlineName();

        // Assert
        assertEquals("IndiGo", airlineName);
    }

    @Test
    void testIsAvailable() {
        // Act
        boolean available = indigoAdapter.isAvailable();

        // Assert
        assertTrue(available);
    }

    @Test
    void testSearchFlights_Success() throws Exception {
        // Act
        CompletableFuture<List<FlightSearchResult>> future = indigoAdapter.searchFlights(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size()); // IndiGo generates 2 flights

        // Verify first flight (morning)
        FlightSearchResult flight1 = results.get(0);
        assertEquals("6E-123", flight1.getFlightNumber());
        assertEquals("IndiGo", flight1.getAirline());
        assertEquals("6E", flight1.getAirlineCode());
        assertEquals("DEL", flight1.getOrigin());
        assertEquals("BOM", flight1.getDestination());
        assertEquals(LocalTime.of(8, 30), flight1.getDepartureTime());
        assertEquals(LocalTime.of(10, 45), flight1.getArrivalTime());
        assertEquals(new BigDecimal("4500"), flight1.getPrice());
        assertEquals("INR", flight1.getCurrency());
        assertEquals(135, flight1.getDurationMinutes());
        assertEquals(0, flight1.getStops());

        // Verify second flight (afternoon)
        FlightSearchResult flight2 = results.get(1);
        assertEquals("6E-456", flight2.getFlightNumber());
        assertEquals(LocalTime.of(14, 15), flight2.getDepartureTime());
        assertEquals(new BigDecimal("5200"), flight2.getPrice());
    }

    @Test
    void testSearchFlights_VerifyFlightDetails() throws Exception {
        // Act
        CompletableFuture<List<FlightSearchResult>> future = indigoAdapter.searchFlights(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert - All flights should have complete details
        for (FlightSearchResult flight : results) {
            assertNotNull(flight.getFlightId());
            assertNotNull(flight.getFlightNumber());
            assertTrue(flight.getFlightNumber().startsWith("6E-"));
            assertEquals("IndiGo", flight.getAirline());
            assertEquals("6E", flight.getAirlineCode());
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
        AirlineBookingResponse response = indigoAdapter.createBooking(
            "flight-123", flightDetails, bookingRequest
        );

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getPnr());
        assertTrue(response.getPnr().startsWith("6E"));
        assertNotNull(response.getBookingId());
        assertEquals("6E", response.getAirlineCode());
        assertNotNull(response.getMessage());
    }

    @Test
    void testCreateBooking_GeneratesPNR() {
        // Act
        AirlineBookingResponse response1 = indigoAdapter.createBooking("flight-1", flightDetails, bookingRequest);
        AirlineBookingResponse response2 = indigoAdapter.createBooking("flight-2", flightDetails, bookingRequest);

        // Assert - PNRs should be unique
        assertNotNull(response1.getPnr());
        assertNotNull(response2.getPnr());
        assertNotEquals(response1.getPnr(), response2.getPnr());
        assertTrue(response1.getPnr().startsWith("6E"));
        assertTrue(response2.getPnr().startsWith("6E"));
    }

    @Test
    void testGetBookingDetails() {
        // Act
        AirlineBookingDetails details = indigoAdapter.getBookingDetails("6E-ABC123");

        // Assert - Mock API returns null, but doesn't throw
        // In a real test with actual API, you'd verify the details
        assertNull(details); // Because mock API is not running
    }

    @Test
    void testCancelBooking() {
        // Act
        boolean result = indigoAdapter.cancelBooking("6E-ABC123");

        // Assert - Returns true as mock fallback
        assertTrue(result);
    }

    @Test
    void testSearchFlights_DifferentRoutes() throws Exception {
        // Test different routes
        String[][] routes = {
            {"DEL", "BOM"},
            {"BOM", "DEL"},
            {"BLR", "HYD"},
            {"CCU", "DEL"}
        };

        for (String[] route : routes) {
            searchRequest.setOrigin(route[0]);
            searchRequest.setDestination(route[1]);

            CompletableFuture<List<FlightSearchResult>> future = indigoAdapter.searchFlights(searchRequest);
            List<FlightSearchResult> results = future.get();

            assertNotNull(results);
            assertEquals(2, results.size());
            
            for (FlightSearchResult flight : results) {
                assertEquals(route[0], flight.getOrigin());
                assertEquals(route[1], flight.getDestination());
            }
        }
    }

    @Test
    void testSearchFlights_DifferentDates() throws Exception {
        // Test different dates
        LocalDate[] dates = {
            LocalDate.of(2025, 11, 15),
            LocalDate.of(2025, 12, 25),
            LocalDate.of(2026, 1, 1)
        };

        for (LocalDate date : dates) {
            searchRequest.setDepartureDate(date);

            CompletableFuture<List<FlightSearchResult>> future = indigoAdapter.searchFlights(searchRequest);
            List<FlightSearchResult> results = future.get();

            assertNotNull(results);
            for (FlightSearchResult flight : results) {
                assertEquals(date, flight.getDate());
            }
        }
    }

    @Test
    void testCreateBooking_WithMultiplePassengers() {
        // Arrange
        PassengerRequest passenger1 = new PassengerRequest("John", "Doe", 30, "ADULT");
        PassengerRequest passenger2 = new PassengerRequest("Jane", "Doe", 28, "ADULT");
        
        bookingRequest.setPassengers(Arrays.asList(passenger1, passenger2));
        bookingRequest.setPassengerCount(2);

        // Act
        AirlineBookingResponse response = indigoAdapter.createBooking(
            "flight-123", flightDetails, bookingRequest
        );

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @Test
    void testPNRFormat() {
        // Act - Generate multiple PNRs
        AirlineBookingResponse response1 = indigoAdapter.createBooking("f1", flightDetails, bookingRequest);
        AirlineBookingResponse response2 = indigoAdapter.createBooking("f2", flightDetails, bookingRequest);
        AirlineBookingResponse response3 = indigoAdapter.createBooking("f3", flightDetails, bookingRequest);

        // Assert - All PNRs should start with 6E and be 8 characters long
        assertEquals(8, response1.getPnr().length());
        assertEquals(8, response2.getPnr().length());
        assertEquals(8, response3.getPnr().length());
        assertTrue(response1.getPnr().startsWith("6E"));
        assertTrue(response2.getPnr().startsWith("6E"));
        assertTrue(response3.getPnr().startsWith("6E"));
    }
}

