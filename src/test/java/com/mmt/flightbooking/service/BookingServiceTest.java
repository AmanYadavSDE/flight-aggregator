package com.mmt.flightbooking.service;

import com.mmt.flightbooking.dto.*;
import com.mmt.flightbooking.entity.*;
import com.mmt.flightbooking.repository.BookingRepository;
import com.mmt.flightbooking.service.airline.AirlineBookingService;
import com.mmt.flightbooking.service.airline.adapter.AirlineBookingResponse;
import com.mmt.flightbooking.service.notification.NotificationService;
import com.mmt.flightbooking.service.payment.PaymentService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FlightSearchCacheService searchCacheService;

    @Mock
    private AirlineBookingService airlineBookingService;

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private CreateBookingRequest createBookingRequest;
    private FlightSearchResult testFlightResult;
    private AirlineBookingResponse airlineResponse;
    private Booking testBooking;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPhone("+919876543210");

        // Setup flight search result
        testFlightResult = new FlightSearchResult();
        testFlightResult.setFlightId("flight-123");
        testFlightResult.setFlightNumber("6E-2001");
        testFlightResult.setAirline("IndiGo");
        testFlightResult.setAirlineCode("6E");
        testFlightResult.setOrigin("DEL");
        testFlightResult.setDestination("BOM");
        testFlightResult.setDate(LocalDate.of(2025, 11, 15));
        testFlightResult.setDepartureTime(LocalTime.of(10, 30));
        testFlightResult.setArrivalTime(LocalTime.of(12, 45));
        testFlightResult.setPrice(new BigDecimal("5500.00"));
        testFlightResult.setCurrency("INR");
        testFlightResult.setDurationMinutes(135);

        // Setup passenger requests
        PassengerRequest passenger = new PassengerRequest();
        passenger.setFirstName("John");
        passenger.setLastName("Doe");
        passenger.setAge(30);
        passenger.setType("ADULT");
        passenger.setPassportNumber("AB123456");
        passenger.setNationality("Indian");

        // Setup create booking request
        createBookingRequest = new CreateBookingRequest();
        createBookingRequest.setFlightIds(Arrays.asList("flight-123"));
        createBookingRequest.setPassengerCount(1);
        createBookingRequest.setPassengers(Arrays.asList(passenger));
        createBookingRequest.setPaymentMethod("CREDIT_CARD");
        createBookingRequest.setContactEmail("test@example.com");
        createBookingRequest.setContactPhone("+919876543210");

        // Setup airline booking response
        airlineResponse = new AirlineBookingResponse();
        airlineResponse.setSuccess(true);
        airlineResponse.setPnr("6E-ABC123");
        airlineResponse.setBookingId("airline-booking-123");
        airlineResponse.setAirlineCode("6E");
        airlineResponse.setMessage("Booking successful");

        // Setup test booking
        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setUser(testUser);
        testBooking.setBookingReference("MMT12345678");
        testBooking.setStatus(BookingStatus.CONFIRMED);
        testBooking.setTotalAmount(new BigDecimal("5500.00"));
        testBooking.setCurrency("INR");
        testBooking.setAirlineCode("6E");
        testBooking.setAirlinePnr("6E-ABC123");
        testBooking.setAirlineBookingId("airline-booking-123");
        testBooking.setFlightNumber("6E-2001");
        testBooking.setOriginAirport("DEL");
        testBooking.setDestinationAirport("BOM");
        testBooking.setDepartureDate(LocalDate.of(2025, 11, 15));

        // Setup test payment
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setBooking(testBooking);
        testPayment.setAmount(new BigDecimal("5500.00"));
        testPayment.setMethod(PaymentMethod.CREDIT_CARD);
        testPayment.setStatus(PaymentStatus.COMPLETED);
    }

    @Test
    void testCreateBooking_Success() {
        // Arrange
        when(searchCacheService.getFlightFromCache("flight-123")).thenReturn(testFlightResult);
        when(airlineBookingService.createBookingWithAirline(
            eq("flight-123"), 
            eq(testFlightResult), 
            eq(createBookingRequest)
        )).thenReturn(airlineResponse);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(paymentService.createPayment(any(Booking.class), any(PaymentMethod.class)))
            .thenReturn(testPayment);

        // Act
        BookingResponse response = bookingService.createBooking(createBookingRequest, testUser);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(1L, response.getBookingId());
        assertEquals("MMT12345678", response.getBookingReference());
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals(new BigDecimal("5500.00"), response.getTotalAmount());
        assertEquals("INR", response.getCurrency());
        assertTrue(response.getMessage().contains("6E-ABC123"));

        // Verify interactions
        verify(searchCacheService, times(1)).getFlightFromCache("flight-123");
        verify(airlineBookingService, times(1)).createBookingWithAirline(
            eq("flight-123"), 
            eq(testFlightResult), 
            eq(createBookingRequest)
        );
        verify(bookingRepository, times(2)).save(any(Booking.class));
        verify(paymentService, times(1)).createPayment(any(Booking.class), eq(PaymentMethod.CREDIT_CARD));
    }

    @Test
    void testCreateBooking_FlightNotFoundInCache() {
        // Arrange
        when(searchCacheService.getFlightFromCache("flight-123")).thenReturn(null);

        // Act
        BookingResponse response = bookingService.createBooking(createBookingRequest, testUser);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Flight not found in search results"));

        // Verify that airline booking was not attempted
        verify(airlineBookingService, never()).createBookingWithAirline(any(), any(), any());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_AirlineBookingFailed() {
        // Arrange
        AirlineBookingResponse failedResponse = new AirlineBookingResponse();
        failedResponse.setSuccess(false);
        failedResponse.setMessage("No seats available");

        when(searchCacheService.getFlightFromCache("flight-123")).thenReturn(testFlightResult);
        when(airlineBookingService.createBookingWithAirline(any(), any(), any()))
            .thenReturn(failedResponse);

        // Act
        BookingResponse response = bookingService.createBooking(createBookingRequest, testUser);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Airline booking failed"));
        assertTrue(response.getMessage().contains("No seats available"));

        // Verify booking was not saved
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_BookingEntityCreatedCorrectly() {
        // Arrange
        when(searchCacheService.getFlightFromCache("flight-123")).thenReturn(testFlightResult);
        when(airlineBookingService.createBookingWithAirline(any(), any(), any()))
            .thenReturn(airlineResponse);
        
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        when(bookingRepository.save(bookingCaptor.capture())).thenReturn(testBooking);
        when(paymentService.createPayment(any(Booking.class), any())).thenReturn(testPayment);

        // Act
        bookingService.createBooking(createBookingRequest, testUser);

        // Assert
        Booking capturedBooking = bookingCaptor.getAllValues().get(0);
        assertEquals(testUser, capturedBooking.getUser());
        assertEquals(BookingStatus.CONFIRMED, capturedBooking.getStatus());
        assertEquals(new BigDecimal("5500.00"), capturedBooking.getTotalAmount());
        assertEquals("INR", capturedBooking.getCurrency());
        assertEquals("6E", capturedBooking.getAirlineCode());
        assertEquals("6E-ABC123", capturedBooking.getAirlinePnr());
        assertEquals("airline-booking-123", capturedBooking.getAirlineBookingId());
        assertEquals("6E-2001", capturedBooking.getFlightNumber());
        assertEquals("DEL", capturedBooking.getOriginAirport());
        assertEquals("BOM", capturedBooking.getDestinationAirport());
        assertEquals(LocalDate.of(2025, 11, 15), capturedBooking.getDepartureDate());
    }

    @Test
    void testCreateBooking_PassengersAddedCorrectly() {
        // Arrange
        when(searchCacheService.getFlightFromCache("flight-123")).thenReturn(testFlightResult);
        when(airlineBookingService.createBookingWithAirline(any(), any(), any()))
            .thenReturn(airlineResponse);
        
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        when(bookingRepository.save(any(Booking.class))).then(invocation -> invocation.getArgument(0));
        when(paymentService.createPayment(any(Booking.class), any())).thenReturn(testPayment);

        // Act
        bookingService.createBooking(createBookingRequest, testUser);

        // Assert
        verify(bookingRepository, atLeast(1)).save(bookingCaptor.capture());
        // Get the first save call which has passengers
        Booking capturedBooking = bookingCaptor.getAllValues().get(0);
        List<Passenger> passengers = capturedBooking.getPassengers();
        assertEquals(1, passengers.size());
        
        Passenger passenger = passengers.get(0);
        assertEquals("John", passenger.getFirstName());
        assertEquals("Doe", passenger.getLastName());
        assertEquals(30, passenger.getAge());
        assertEquals(PassengerType.ADULT, passenger.getType());
        assertEquals("AB123456", passenger.getPassportNumber());
        assertEquals("Indian", passenger.getNationality());
    }

    @Test
    void testConfirmBooking_Success() {
        // Arrange
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPayment(testPayment);

        PaymentResult paymentResult = new PaymentResult();
        paymentResult.setSuccess(true);
        paymentResult.setTransactionId("txn-123");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(paymentService.processPayment(paymentRequest)).thenReturn(paymentResult);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // Act
        BookingResponse response = bookingService.confirmBooking(1L, paymentRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(1L, response.getBookingId());
        assertEquals("MMT12345678", response.getBookingReference());
        assertEquals("CONFIRMED", response.getStatus());

        verify(notificationService, times(1)).sendBookingConfirmation(testBooking);
        verify(bookingRepository, times(1)).save(testBooking);
    }

    @Test
    void testConfirmBooking_PaymentFailed() {
        // Arrange
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPayment(testPayment);

        PaymentResult paymentResult = new PaymentResult();
        paymentResult.setSuccess(false);
        paymentResult.setMessage("Insufficient funds");

        testBooking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(paymentService.processPayment(paymentRequest)).thenReturn(paymentResult);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // Act
        BookingResponse response = bookingService.confirmBooking(1L, paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Payment failed"));
        assertTrue(response.getMessage().contains("Insufficient funds"));

        verify(notificationService, never()).sendBookingConfirmation(any());
        
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertEquals(BookingStatus.FAILED, bookingCaptor.getValue().getStatus());
    }

    @Test
    void testConfirmBooking_BookingNotFound() {
        // Arrange
        Payment nonExistentPayment = new Payment();
        nonExistentPayment.setBooking(testBooking);
        
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPayment(nonExistentPayment);

        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        BookingResponse response = bookingService.confirmBooking(999L, paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("failed"));

        verify(paymentService, never()).processPayment(any());
        verify(notificationService, never()).sendBookingConfirmation(any());
    }

    @Test
    void testGetBookingById_Success() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        // Act
        Booking result = bookingService.getBookingById(1L, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("MMT12345678", result.getBookingReference());
        verify(bookingRepository, times(1)).findById(1L);
    }

    @Test
    void testGetBookingById_NotFound() {
        // Arrange
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            bookingService.getBookingById(999L, testUser);
        });
    }

    @Test
    void testGetBookingById_WrongUser() {
        // Arrange
        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setEmail("other@example.com");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            bookingService.getBookingById(1L, differentUser);
        });
    }

    @Test
    void testGetUserBookings_Success() {
        // Arrange
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findUserBookings(testUser)).thenReturn(bookings);

        // Act
        List<Booking> result = bookingService.getUserBookings(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBooking.getId(), result.get(0).getId());
        verify(bookingRepository, times(1)).findUserBookings(testUser);
    }

    @Test
    void testCreateBooking_ExceptionHandling() {
        // Arrange
        when(searchCacheService.getFlightFromCache(any()))
            .thenThrow(new RuntimeException("Redis connection failed"));

        // Act
        BookingResponse response = bookingService.createBooking(createBookingRequest, testUser);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Booking creation failed"));
        assertTrue(response.getMessage().contains("Redis connection failed"));
    }

    @Test
    void testCreateBooking_WithMultiplePassengers() {
        // Arrange
        PassengerRequest passenger1 = new PassengerRequest("John", "Doe", 30, "ADULT");
        PassengerRequest passenger2 = new PassengerRequest("Jane", "Doe", 28, "ADULT");
        PassengerRequest passenger3 = new PassengerRequest("Jimmy", "Doe", 5, "CHILD");

        createBookingRequest.setPassengers(Arrays.asList(passenger1, passenger2, passenger3));
        createBookingRequest.setPassengerCount(3);

        when(searchCacheService.getFlightFromCache("flight-123")).thenReturn(testFlightResult);
        when(airlineBookingService.createBookingWithAirline(any(), any(), any()))
            .thenReturn(airlineResponse);
        
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        when(bookingRepository.save(any(Booking.class))).then(invocation -> invocation.getArgument(0));
        when(paymentService.createPayment(any(Booking.class), any())).thenReturn(testPayment);

        // Act
        bookingService.createBooking(createBookingRequest, testUser);

        // Assert
        verify(bookingRepository, atLeast(1)).save(bookingCaptor.capture());
        Booking capturedBooking = bookingCaptor.getAllValues().get(0);
        assertEquals(3, capturedBooking.getPassengers().size());
    }
}

