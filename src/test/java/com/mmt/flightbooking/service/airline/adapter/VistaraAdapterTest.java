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
class VistaraAdapterTest {

    private VistaraAdapter vistaraAdapter;
    private FlightSearchRequest searchRequest;
    private CreateBookingRequest bookingRequest;
    private FlightSearchResult flightDetails;

    @BeforeEach
    void setUp() {
        vistaraAdapter = new VistaraAdapter();
        ReflectionTestUtils.setField(vistaraAdapter, "apiBaseUrl", "http://mock-vistara-api:8080");

        // Setup search request
        searchRequest = new FlightSearchRequest();
        searchRequest.setOrigin("DEL");
        searchRequest.setDestination("BOM");
        searchRequest.setDepartureDate(LocalDate.of(2025, 11, 15));
        searchRequest.setPassengerCount(1);

        // Setup flight details
        flightDetails = new FlightSearchResult();
        flightDetails.setFlightId("flight-999");
        flightDetails.setFlightNumber("UK-941");
        flightDetails.setAirline("Vistara");
        flightDetails.setAirlineCode("UK");
        flightDetails.setOrigin("DEL");
        flightDetails.setDestination("BOM");
        flightDetails.setDate(LocalDate.of(2025, 11, 15));
        flightDetails.setDepartureTime(LocalTime.of(16, 40));
        flightDetails.setPrice(new BigDecimal("6800.00"));

        // Setup booking request
        PassengerRequest passenger = new PassengerRequest();
        passenger.setFirstName("John");
        passenger.setLastName("Doe");
        passenger.setAge(30);
        passenger.setType("ADULT");

        bookingRequest = new CreateBookingRequest();
        bookingRequest.setFlightIds(Arrays.asList("flight-999"));
        bookingRequest.setPassengerCount(1);
        bookingRequest.setPassengers(Arrays.asList(passenger));
        bookingRequest.setContactEmail("test@example.com");
        bookingRequest.setContactPhone("+919876543210");
    }

    @Test
    void testGetAirlineCode() {
        // Act
        String airlineCode = vistaraAdapter.getAirlineCode();

        // Assert
        assertEquals("UK", airlineCode);
    }

    @Test
    void testGetAirlineName() {
        // Act
        String airlineName = vistaraAdapter.getAirlineName();

        // Assert
        assertEquals("Vistara", airlineName);
    }

    @Test
    void testIsAvailable() {
        // Act
        boolean available = vistaraAdapter.isAvailable();

        // Assert
        assertTrue(available);
    }

    @Test
    void testSearchFlights_Success() throws Exception {
        // Act
        CompletableFuture<List<FlightSearchResult>> future = vistaraAdapter.searchFlights(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size()); // Vistara generates 1 flight

        FlightSearchResult flight = results.get(0);
        assertEquals("UK-567", flight.getFlightNumber());
        assertEquals("Vistara", flight.getAirline());
        assertEquals("UK", flight.getAirlineCode());
        assertEquals("DEL", flight.getOrigin());
        assertEquals("BOM", flight.getDestination());
        assertEquals(LocalTime.of(16, 40), flight.getDepartureTime());
        assertEquals(LocalTime.of(18, 55), flight.getArrivalTime());
        assertEquals(new BigDecimal("6800"), flight.getPrice());
        assertEquals("INR", flight.getCurrency());
        assertEquals(135, flight.getDurationMinutes());
    }

    @Test
    void testSearchFlights_VerifyAllFields() throws Exception {
        // Act
        CompletableFuture<List<FlightSearchResult>> future = vistaraAdapter.searchFlights(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        FlightSearchResult flight = results.get(0);
        assertNotNull(flight.getFlightId());
        assertTrue(flight.getFlightNumber().startsWith("UK-"));
        assertEquals("Vistara", flight.getAirline());
        assertEquals("UK", flight.getAirlineCode());
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

    @Test
    void testCreateBooking_Success() {
        // Act
        AirlineBookingResponse response = vistaraAdapter.createBooking(
            "flight-999", flightDetails, bookingRequest
        );

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getPnr());
        assertTrue(response.getPnr().startsWith("UK"));
        assertNotNull(response.getBookingId());
        assertEquals("UK", response.getAirlineCode());
        assertNotNull(response.getMessage());
    }

    @Test
    void testCreateBooking_GeneratesUniquePNRs() {
        // Act
        AirlineBookingResponse response1 = vistaraAdapter.createBooking("f1", flightDetails, bookingRequest);
        AirlineBookingResponse response2 = vistaraAdapter.createBooking("f2", flightDetails, bookingRequest);
        AirlineBookingResponse response3 = vistaraAdapter.createBooking("f3", flightDetails, bookingRequest);

        // Assert
        assertNotEquals(response1.getPnr(), response2.getPnr());
        assertNotEquals(response2.getPnr(), response3.getPnr());
        assertNotEquals(response1.getPnr(), response3.getPnr());
    }

    @Test
    void testPNRFormat() {
        // Act
        AirlineBookingResponse response = vistaraAdapter.createBooking(
            "flight-999", flightDetails, bookingRequest
        );

        // Assert - PNR should be UK + 6 alphanumeric characters
        assertEquals(8, response.getPnr().length());
        assertTrue(response.getPnr().startsWith("UK"));
        assertTrue(response.getPnr().substring(2).matches("[A-Z0-9]{6}"));
    }

    @Test
    void testGetBookingDetails() {
        // Act
        AirlineBookingDetails details = vistaraAdapter.getBookingDetails("UK-ABC123");

        // Assert
        assertNull(details); // Mock API not running
    }

    @Test
    void testCancelBooking() {
        // Act
        boolean result = vistaraAdapter.cancelBooking("UK-ABC123");

        // Assert
        assertTrue(result); // Mock fallback
    }

    @Test
    void testSearchFlights_DifferentRoutes() throws Exception {
        // Arrange
        searchRequest.setOrigin("HYD");
        searchRequest.setDestination("CCU");

        // Act
        CompletableFuture<List<FlightSearchResult>> future = vistaraAdapter.searchFlights(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        assertNotNull(results);
        for (FlightSearchResult flight : results) {
            assertEquals("HYD", flight.getOrigin());
            assertEquals("CCU", flight.getDestination());
        }
    }

    @Test
    void testSearchFlights_DifferentDates() throws Exception {
        // Arrange
        searchRequest.setDepartureDate(LocalDate.of(2025, 12, 25));

        // Act
        CompletableFuture<List<FlightSearchResult>> future = vistaraAdapter.searchFlights(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        assertNotNull(results);
        for (FlightSearchResult flight : results) {
            assertEquals(LocalDate.of(2025, 12, 25), flight.getDate());
        }
    }

    @Test
    void testCreateBooking_WithMultiplePassengers() {
        // Arrange
        PassengerRequest passenger1 = new PassengerRequest("John", "Doe", 30, "ADULT");
        PassengerRequest passenger2 = new PassengerRequest("Jane", "Doe", 28, "ADULT");
        PassengerRequest passenger3 = new PassengerRequest("Jimmy", "Doe", 3, "INFANT");
        
        bookingRequest.setPassengers(Arrays.asList(passenger1, passenger2, passenger3));
        bookingRequest.setPassengerCount(3);

        // Act
        AirlineBookingResponse response = vistaraAdapter.createBooking(
            "flight-999", flightDetails, bookingRequest
        );

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }

    @Test
    void testSearchFlights_AsyncExecution() {
        // Act
        CompletableFuture<List<FlightSearchResult>> future = vistaraAdapter.searchFlights(searchRequest);

        // Assert
        assertNotNull(future);
        assertFalse(future.isCancelled());
    }

    @Test
    void testSearchFlights_PremiumPricing() throws Exception {
        // Act
        CompletableFuture<List<FlightSearchResult>> future = vistaraAdapter.searchFlights(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert - Vistara typically has premium pricing
        FlightSearchResult flight = results.get(0);
        assertTrue(flight.getPrice().compareTo(new BigDecimal("6000")) > 0);
    }
}

