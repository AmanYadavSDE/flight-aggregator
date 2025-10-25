package com.mmt.flightbooking.service.airline;

import com.mmt.flightbooking.dto.CreateBookingRequest;
import com.mmt.flightbooking.dto.FlightSearchResult;
import com.mmt.flightbooking.dto.PassengerRequest;
import com.mmt.flightbooking.service.airline.adapter.AirlineAdapter;
import com.mmt.flightbooking.service.airline.adapter.AirlineAdapterFactory;
import com.mmt.flightbooking.service.airline.adapter.AirlineBookingDetails;
import com.mmt.flightbooking.service.airline.adapter.AirlineBookingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AirlineBookingServiceTest {

    @Mock
    private AirlineAdapterFactory adapterFactory;

    @Mock
    private AirlineAdapter mockAdapter;

    @InjectMocks
    private AirlineBookingService airlineBookingService;

    private FlightSearchResult flightDetails;
    private CreateBookingRequest bookingRequest;
    private AirlineBookingResponse successResponse;

    @BeforeEach
    void setUp() {
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
        bookingRequest.setPaymentMethod("CREDIT_CARD");
        bookingRequest.setContactEmail("test@example.com");

        // Setup success response
        successResponse = new AirlineBookingResponse();
        successResponse.setSuccess(true);
        successResponse.setPnr("6E-ABC123");
        successResponse.setBookingId("airline-booking-123");
        successResponse.setAirlineCode("6E");
        successResponse.setMessage("Booking successful");
    }

    @Test
    void testCreateBookingWithAirline_Success() {
        // Arrange
        when(adapterFactory.getAdapter("6E")).thenReturn(mockAdapter);
        when(mockAdapter.createBooking(eq("flight-123"), eq(flightDetails), eq(bookingRequest)))
            .thenReturn(successResponse);

        // Act
        AirlineBookingResponse response = airlineBookingService.createBookingWithAirline(
            "flight-123", flightDetails, bookingRequest
        );

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("6E-ABC123", response.getPnr());
        assertEquals("airline-booking-123", response.getBookingId());
        assertEquals("6E", response.getAirlineCode());

        verify(adapterFactory, times(1)).getAdapter("6E");
        verify(mockAdapter, times(1)).createBooking("flight-123", flightDetails, bookingRequest);
    }

    @Test
    void testCreateBookingWithAirline_UnsupportedAirline() {
        // Arrange
        when(adapterFactory.getAdapter("XX"))
            .thenThrow(new IllegalArgumentException("Unsupported airline code: XX"));

        flightDetails.setAirlineCode("XX");

        // Act
        AirlineBookingResponse response = airlineBookingService.createBookingWithAirline(
            "flight-123", flightDetails, bookingRequest
        );

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Unsupported airline"));
        assertEquals("XX", response.getAirlineCode());

        verify(mockAdapter, never()).createBooking(any(), any(), any());
    }

    @Test
    void testCreateBookingWithAirline_AdapterFails() {
        // Arrange
        when(adapterFactory.getAdapter("6E")).thenReturn(mockAdapter);
        when(mockAdapter.createBooking(any(), any(), any()))
            .thenThrow(new RuntimeException("Airline API error"));

        // Act
        AirlineBookingResponse response = airlineBookingService.createBookingWithAirline(
            "flight-123", flightDetails, bookingRequest
        );

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Booking failed"));
        assertTrue(response.getMessage().contains("Airline API error"));
    }

    @Test
    void testCreateBookingWithAirline_DifferentAirlines() {
        // Test with different airline codes
        String[][] airlines = {
            {"6E", "IndiGo"},
            {"AI", "Air India"},
            {"SG", "SpiceJet"},
            {"UK", "Vistara"}
        };

        for (String[] airline : airlines) {
            String code = airline[0];
            String name = airline[1];

            flightDetails.setAirlineCode(code);
            flightDetails.setAirline(name);

            AirlineBookingResponse mockResponse = new AirlineBookingResponse();
            mockResponse.setSuccess(true);
            mockResponse.setPnr(code + "-ABC123");
            mockResponse.setAirlineCode(code);

            when(adapterFactory.getAdapter(code)).thenReturn(mockAdapter);
            when(mockAdapter.createBooking(any(), any(), any())).thenReturn(mockResponse);

            AirlineBookingResponse response = airlineBookingService.createBookingWithAirline(
                "flight-123", flightDetails, bookingRequest
            );

            assertTrue(response.isSuccess());
            assertEquals(code, response.getAirlineCode());
        }
    }

    @Test
    void testGetBookingFromAirline_Success() {
        // Arrange
        AirlineBookingDetails bookingDetails = new AirlineBookingDetails();
        bookingDetails.setPnr("6E-ABC123");
        bookingDetails.setStatus("CONFIRMED");
        bookingDetails.setFlightNumber("6E-2001");

        when(adapterFactory.getAdapter("6E")).thenReturn(mockAdapter);
        when(mockAdapter.getBookingDetails("6E-ABC123")).thenReturn(bookingDetails);

        // Act
        AirlineBookingDetails result = airlineBookingService.getBookingFromAirline("6E", "6E-ABC123");

        // Assert
        assertNotNull(result);
        assertEquals("6E-ABC123", result.getPnr());
        assertEquals("CONFIRMED", result.getStatus());
        assertEquals("6E-2001", result.getFlightNumber());

        verify(adapterFactory, times(1)).getAdapter("6E");
        verify(mockAdapter, times(1)).getBookingDetails("6E-ABC123");
    }

    @Test
    void testGetBookingFromAirline_NotFound() {
        // Arrange
        when(adapterFactory.getAdapter("6E")).thenReturn(mockAdapter);
        when(mockAdapter.getBookingDetails("INVALID-PNR")).thenReturn(null);

        // Act
        AirlineBookingDetails result = airlineBookingService.getBookingFromAirline("6E", "INVALID-PNR");

        // Assert
        assertNull(result);
    }

    @Test
    void testGetBookingFromAirline_ExceptionHandling() {
        // Arrange
        when(adapterFactory.getAdapter("6E"))
            .thenThrow(new RuntimeException("Adapter error"));

        // Act
        AirlineBookingDetails result = airlineBookingService.getBookingFromAirline("6E", "6E-ABC123");

        // Assert
        assertNull(result);
    }

    @Test
    void testCancelBookingWithAirline_Success() {
        // Arrange
        when(adapterFactory.getAdapter("6E")).thenReturn(mockAdapter);
        when(mockAdapter.cancelBooking("6E-ABC123")).thenReturn(true);

        // Act
        boolean result = airlineBookingService.cancelBookingWithAirline("6E", "6E-ABC123");

        // Assert
        assertTrue(result);
        verify(adapterFactory, times(1)).getAdapter("6E");
        verify(mockAdapter, times(1)).cancelBooking("6E-ABC123");
    }

    @Test
    void testCancelBookingWithAirline_Failed() {
        // Arrange
        when(adapterFactory.getAdapter("6E")).thenReturn(mockAdapter);
        when(mockAdapter.cancelBooking("6E-ABC123")).thenReturn(false);

        // Act
        boolean result = airlineBookingService.cancelBookingWithAirline("6E", "6E-ABC123");

        // Assert
        assertFalse(result);
    }

    @Test
    void testCancelBookingWithAirline_ExceptionHandling() {
        // Arrange
        when(adapterFactory.getAdapter("6E"))
            .thenThrow(new RuntimeException("Adapter error"));

        // Act
        boolean result = airlineBookingService.cancelBookingWithAirline("6E", "6E-ABC123");

        // Assert
        assertFalse(result);
    }

    @Test
    void testCancelBookingWithAirline_DifferentAirlines() {
        // Test cancellation with different airlines
        String[] airlineCodes = {"6E", "AI", "SG", "UK"};

        for (String code : airlineCodes) {
            when(adapterFactory.getAdapter(code)).thenReturn(mockAdapter);
            when(mockAdapter.cancelBooking(anyString())).thenReturn(true);

            boolean result = airlineBookingService.cancelBookingWithAirline(code, code + "-PNR123");

            assertTrue(result);
        }
    }
}

