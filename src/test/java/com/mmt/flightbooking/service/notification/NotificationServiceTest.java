package com.mmt.flightbooking.service.notification;

import com.mmt.flightbooking.entity.Booking;
import com.mmt.flightbooking.entity.BookingStatus;
import com.mmt.flightbooking.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private BookingEventPublisher eventPublisher;

    @InjectMocks
    private NotificationService notificationService;

    private Booking testBooking;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPhone("+919876543210");

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
        testBooking.setFlightNumber("6E-2001");
        testBooking.setOriginAirport("DEL");
        testBooking.setDestinationAirport("BOM");
        testBooking.setDepartureDate(LocalDate.of(2025, 11, 15));
    }

    @Test
    void testSendBookingConfirmation_Success() {
        // Act
        notificationService.sendBookingConfirmation(testBooking);

        // Assert
        verify(eventPublisher, times(1)).publishBookingConfirmed(testBooking);
    }

    @Test
    void testSendBookingConfirmation_WithDifferentBookings() {
        // Arrange
        Booking booking1 = createBooking("MMT11111111", "6E-ABC123");
        Booking booking2 = createBooking("MMT22222222", "AI-XYZ789");
        Booking booking3 = createBooking("MMT33333333", "SG-DEF456");

        // Act
        notificationService.sendBookingConfirmation(booking1);
        notificationService.sendBookingConfirmation(booking2);
        notificationService.sendBookingConfirmation(booking3);

        // Assert
        verify(eventPublisher, times(1)).publishBookingConfirmed(booking1);
        verify(eventPublisher, times(1)).publishBookingConfirmed(booking2);
        verify(eventPublisher, times(1)).publishBookingConfirmed(booking3);
        verify(eventPublisher, times(3)).publishBookingConfirmed(any(Booking.class));
    }

    @Test
    void testSendPaymentConfirmation_Success() {
        // Act
        notificationService.sendPaymentConfirmation(testBooking);

        // Assert
        verify(eventPublisher, times(1)).publishPaymentConfirmed(testBooking);
    }

    @Test
    void testSendPaymentConfirmation_WithDifferentBookings() {
        // Arrange
        Booking booking1 = createBooking("MMT11111111", "6E-ABC123");
        Booking booking2 = createBooking("MMT22222222", "AI-XYZ789");

        // Act
        notificationService.sendPaymentConfirmation(booking1);
        notificationService.sendPaymentConfirmation(booking2);

        // Assert
        verify(eventPublisher, times(1)).publishPaymentConfirmed(booking1);
        verify(eventPublisher, times(1)).publishPaymentConfirmed(booking2);
        verify(eventPublisher, times(2)).publishPaymentConfirmed(any(Booking.class));
    }

    @Test
    void testSendBookingCancellation_Success() {
        // Arrange
        testBooking.setStatus(BookingStatus.CANCELLED);

        // Act
        notificationService.sendBookingCancellation(testBooking);

        // Assert
        verify(eventPublisher, times(1)).publishBookingCancelled(testBooking);
    }

    @Test
    void testSendBookingCancellation_WithDifferentBookings() {
        // Arrange
        Booking booking1 = createBooking("MMT11111111", "6E-ABC123");
        booking1.setStatus(BookingStatus.CANCELLED);
        
        Booking booking2 = createBooking("MMT22222222", "AI-XYZ789");
        booking2.setStatus(BookingStatus.CANCELLED);

        // Act
        notificationService.sendBookingCancellation(booking1);
        notificationService.sendBookingCancellation(booking2);

        // Assert
        verify(eventPublisher, times(1)).publishBookingCancelled(booking1);
        verify(eventPublisher, times(1)).publishBookingCancelled(booking2);
        verify(eventPublisher, times(2)).publishBookingCancelled(any(Booking.class));
    }

    @Test
    void testMultipleNotificationTypes_ForSameBooking() {
        // Act
        notificationService.sendBookingConfirmation(testBooking);
        notificationService.sendPaymentConfirmation(testBooking);

        // Assert
        verify(eventPublisher, times(1)).publishBookingConfirmed(testBooking);
        verify(eventPublisher, times(1)).publishPaymentConfirmed(testBooking);
        verify(eventPublisher, never()).publishBookingCancelled(any());
    }

    @Test
    void testAllNotificationTypes_ForDifferentBookings() {
        // Arrange
        Booking confirmedBooking = createBooking("MMT11111111", "6E-ABC123");
        Booking paidBooking = createBooking("MMT22222222", "AI-XYZ789");
        Booking cancelledBooking = createBooking("MMT33333333", "SG-DEF456");
        cancelledBooking.setStatus(BookingStatus.CANCELLED);

        // Act
        notificationService.sendBookingConfirmation(confirmedBooking);
        notificationService.sendPaymentConfirmation(paidBooking);
        notificationService.sendBookingCancellation(cancelledBooking);

        // Assert
        verify(eventPublisher, times(1)).publishBookingConfirmed(confirmedBooking);
        verify(eventPublisher, times(1)).publishPaymentConfirmed(paidBooking);
        verify(eventPublisher, times(1)).publishBookingCancelled(cancelledBooking);
    }

    @Test
    void testSendBookingConfirmation_NullBooking() {
        // Act & Assert - should throw NullPointerException
        assertThrows(NullPointerException.class, () -> {
            notificationService.sendBookingConfirmation(null);
        });
    }

    @Test
    void testSendPaymentConfirmation_NullBooking() {
        // Act & Assert - should throw NullPointerException
        assertThrows(NullPointerException.class, () -> {
            notificationService.sendPaymentConfirmation(null);
        });
    }

    @Test
    void testSendBookingCancellation_NullBooking() {
        // Act & Assert - should throw NullPointerException
        assertThrows(NullPointerException.class, () -> {
            notificationService.sendBookingCancellation(null);
        });
    }

    @Test
    void testNotificationService_DoesNotCatchPublisherExceptions() {
        // Arrange
        doThrow(new RuntimeException("Publisher failed"))
            .when(eventPublisher).publishBookingConfirmed(any());

        // Act & Assert - exception should propagate
        try {
            notificationService.sendBookingConfirmation(testBooking);
        } catch (RuntimeException e) {
            org.junit.jupiter.api.Assertions.assertEquals("Publisher failed", e.getMessage());
        }

        verify(eventPublisher, times(1)).publishBookingConfirmed(testBooking);
    }

    @Test
    void testBookingConfirmation_WithPendingStatus() {
        // Arrange
        testBooking.setStatus(BookingStatus.PENDING);

        // Act
        notificationService.sendBookingConfirmation(testBooking);

        // Assert - should still send notification regardless of status
        verify(eventPublisher, times(1)).publishBookingConfirmed(testBooking);
    }

    @Test
    void testBookingConfirmation_WithFailedStatus() {
        // Arrange
        testBooking.setStatus(BookingStatus.FAILED);

        // Act
        notificationService.sendBookingConfirmation(testBooking);

        // Assert - should still send notification regardless of status
        verify(eventPublisher, times(1)).publishBookingConfirmed(testBooking);
    }

    @Test
    void testSequentialNotifications() {
        // Simulate typical booking flow
        // 1. Booking created
        notificationService.sendBookingConfirmation(testBooking);
        
        // 2. Payment confirmed
        notificationService.sendPaymentConfirmation(testBooking);

        // Verify sequence
        verify(eventPublisher, times(1)).publishBookingConfirmed(testBooking);
        verify(eventPublisher, times(1)).publishPaymentConfirmed(testBooking);
    }

    @Test
    void testCancellationAfterConfirmation() {
        // Simulate booking then cancellation
        // 1. Booking confirmed
        notificationService.sendBookingConfirmation(testBooking);
        
        // 2. Payment confirmed
        notificationService.sendPaymentConfirmation(testBooking);
        
        // 3. Booking cancelled
        testBooking.setStatus(BookingStatus.CANCELLED);
        notificationService.sendBookingCancellation(testBooking);

        // Verify all notifications were sent
        verify(eventPublisher, times(1)).publishBookingConfirmed(testBooking);
        verify(eventPublisher, times(1)).publishPaymentConfirmed(testBooking);
        verify(eventPublisher, times(1)).publishBookingCancelled(testBooking);
    }

    @Test
    void testMultipleCancellations() {
        // Arrange
        testBooking.setStatus(BookingStatus.CANCELLED);

        // Act - send cancellation multiple times
        notificationService.sendBookingCancellation(testBooking);
        notificationService.sendBookingCancellation(testBooking);

        // Assert - should publish twice
        verify(eventPublisher, times(2)).publishBookingCancelled(testBooking);
    }

    @Test
    void testNotificationWithDifferentAirlines() {
        // Arrange
        Booking indigoBooking = createBooking("MMT11111111", "6E-ABC123");
        indigoBooking.setAirlineCode("6E");

        Booking airIndiaBooking = createBooking("MMT22222222", "AI-XYZ789");
        airIndiaBooking.setAirlineCode("AI");

        Booking spiceJetBooking = createBooking("MMT33333333", "SG-DEF456");
        spiceJetBooking.setAirlineCode("SG");

        // Act
        notificationService.sendBookingConfirmation(indigoBooking);
        notificationService.sendBookingConfirmation(airIndiaBooking);
        notificationService.sendBookingConfirmation(spiceJetBooking);

        // Assert
        verify(eventPublisher, times(1)).publishBookingConfirmed(indigoBooking);
        verify(eventPublisher, times(1)).publishBookingConfirmed(airIndiaBooking);
        verify(eventPublisher, times(1)).publishBookingConfirmed(spiceJetBooking);
    }

    // Helper method to create test bookings
    private Booking createBooking(String bookingRef, String pnr) {
        Booking booking = new Booking();
        booking.setId((long) bookingRef.hashCode());
        booking.setUser(testUser);
        booking.setBookingReference(bookingRef);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalAmount(new BigDecimal("5500.00"));
        booking.setCurrency("INR");
        booking.setAirlinePnr(pnr);
        booking.setFlightNumber("6E-2001");
        booking.setOriginAirport("DEL");
        booking.setDestinationAirport("BOM");
        booking.setDepartureDate(LocalDate.of(2025, 11, 15));
        return booking;
    }
}

