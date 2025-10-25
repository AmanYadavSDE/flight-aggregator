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
class SpiceJetAdapterTest {

    private SpiceJetAdapter spiceJetAdapter;
    private FlightSearchRequest searchRequest;
    private CreateBookingRequest bookingRequest;
    private FlightSearchResult flightDetails;

    @BeforeEach
    void setUp() {
        spiceJetAdapter = new SpiceJetAdapter();
        ReflectionTestUtils.setField(spiceJetAdapter, "apiBaseUrl", "http://mock-spicejet-api:8080");

        // Setup search request
        searchRequest = new FlightSearchRequest();
        searchRequest.setOrigin("DEL");
        searchRequest.setDestination("BOM");
        searchRequest.setDepartureDate(LocalDate.of(2025, 11, 15));
        searchRequest.setPassengerCount(1);

        // Setup flight details
        flightDetails = new FlightSearchResult();
        flightDetails.setFlightId("flight-789");
        flightDetails.setFlightNumber("SG-8156");
        flightDetails.setAirline("SpiceJet");
        flightDetails.setAirlineCode("SG");
        flightDetails.setOrigin("DEL");
        flightDetails.setDestination("BOM");
        flightDetails.setDate(LocalDate.of(2025, 11, 15));
        flightDetails.setDepartureTime(LocalTime.of(11, 10));
        flightDetails.setPrice(new BigDecimal("4200.00"));

        // Setup booking request
        PassengerRequest passenger = new PassengerRequest();
        passenger.setFirstName("John");
        passenger.setLastName("Doe");
        passenger.setAge(30);
        passenger.setType("ADULT");

        bookingRequest = new CreateBookingRequest();
        bookingRequest.setFlightIds(Arrays.asList("flight-789"));
        bookingRequest.setPassengerCount(1);
        bookingRequest.setPassengers(Arrays.asList(passenger));
        bookingRequest.setContactEmail("test@example.com");
        bookingRequest.setContactPhone("+919876543210");
    }

    @Test
    void testGetAirlineCode() {
        // Act
        String airlineCode = spiceJetAdapter.getAirlineCode();

        // Assert
        assertEquals("SG", airlineCode);
    }

    @Test
    void testGetAirlineName() {
        // Act
        String airlineName = spiceJetAdapter.getAirlineName();

        // Assert
        assertEquals("SpiceJet", airlineName);
    }

    @Test
    void testIsAvailable() {
        // Act
        boolean available = spiceJetAdapter.isAvailable();

        // Assert
        assertTrue(available);
    }

    @Test
    void testSearchFlights_Success() throws Exception {
        // Act
        CompletableFuture<List<FlightSearchResult>> future = spiceJetAdapter.searchFlights(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size()); // SpiceJet generates 1 flight

        FlightSearchResult flight = results.get(0);
        assertEquals("SG-234", flight.getFlightNumber());
        assertEquals("SpiceJet", flight.getAirline());
        assertEquals("SG", flight.getAirlineCode());
        assertEquals("DEL", flight.getOrigin());
        assertEquals("BOM", flight.getDestination());
        assertEquals(LocalTime.of(11, 10), flight.getDepartureTime());
        assertEquals(LocalTime.of(13, 25), flight.getArrivalTime());
        assertEquals(new BigDecimal("4200"), flight.getPrice());
        assertEquals("INR", flight.getCurrency());
        assertEquals(135, flight.getDurationMinutes());
    }

    @Test
    void testSearchFlights_VerifyFlightDetails() throws Exception {
        // Act
        CompletableFuture<List<FlightSearchResult>> future = spiceJetAdapter.searchFlights(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        FlightSearchResult flight = results.get(0);
        assertNotNull(flight.getFlightId());
        assertNotNull(flight.getFlightNumber());
        assertTrue(flight.getFlightNumber().startsWith("SG-"));
        assertEquals("SpiceJet", flight.getAirline());
        assertEquals("SG", flight.getAirlineCode());
        assertEquals(LocalDate.of(2025, 11, 15), flight.getDate());
        assertNotNull(flight.getDepartureTime());
        assertNotNull(flight.getArrivalTime());
        assertNotNull(flight.getPrice());
        assertEquals(0, flight.getStops());
        assertEquals(50, flight.getAvailableSeats());
        assertEquals("A320", flight.getAircraftType());
        assertEquals("AVAILABLE", flight.getStatus());
    }

    @Test
    void testCreateBooking_Success() {
        // Act
        AirlineBookingResponse response = spiceJetAdapter.createBooking(
            "flight-789", flightDetails, bookingRequest
        );

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getPnr());
        assertTrue(response.getPnr().startsWith("SG"));
        assertNotNull(response.getBookingId());
        assertEquals("SG", response.getAirlineCode());
        assertNotNull(response.getMessage());
    }

    @Test
    void testCreateBooking_GeneratesUniquePNRs() {
        // Act
        AirlineBookingResponse response1 = spiceJetAdapter.createBooking("f1", flightDetails, bookingRequest);
        AirlineBookingResponse response2 = spiceJetAdapter.createBooking("f2", flightDetails, bookingRequest);

        // Assert
        assertNotEquals(response1.getPnr(), response2.getPnr());
        assertTrue(response1.getPnr().startsWith("SG"));
        assertTrue(response2.getPnr().startsWith("SG"));
    }

    @Test
    void testPNRFormat() {
        // Act
        AirlineBookingResponse response = spiceJetAdapter.createBooking(
            "flight-789", flightDetails, bookingRequest
        );

        // Assert - PNR should be SG + 6 alphanumeric characters
        assertEquals(8, response.getPnr().length());
        assertTrue(response.getPnr().startsWith("SG"));
        assertTrue(response.getPnr().substring(2).matches("[A-Z0-9]{6}"));
    }

    @Test
    void testGetBookingDetails() {
        // Act
        AirlineBookingDetails details = spiceJetAdapter.getBookingDetails("SG-ABC123");

        // Assert
        assertNull(details); // Mock API not running
    }

    @Test
    void testCancelBooking() {
        // Act
        boolean result = spiceJetAdapter.cancelBooking("SG-ABC123");

        // Assert
        assertTrue(result); // Mock fallback
    }

    @Test
    void testSearchFlights_DifferentRoutes() throws Exception {
        // Arrange
        searchRequest.setOrigin("BLR");
        searchRequest.setDestination("CCU");

        // Act
        CompletableFuture<List<FlightSearchResult>> future = spiceJetAdapter.searchFlights(searchRequest);
        List<FlightSearchResult> results = future.get();

        // Assert
        assertNotNull(results);
        for (FlightSearchResult flight : results) {
            assertEquals("BLR", flight.getOrigin());
            assertEquals("CCU", flight.getDestination());
        }
    }

    @Test
    void testCreateBooking_WithMultiplePassengers() {
        // Arrange
        PassengerRequest passenger1 = new PassengerRequest("John", "Doe", 30, "ADULT");
        PassengerRequest passenger2 = new PassengerRequest("Jane", "Doe", 5, "CHILD");
        
        bookingRequest.setPassengers(Arrays.asList(passenger1, passenger2));
        bookingRequest.setPassengerCount(2);

        // Act
        AirlineBookingResponse response = spiceJetAdapter.createBooking(
            "flight-789", flightDetails, bookingRequest
        );

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
    }
}

