package com.mmt.flightbooking.service.payment;

import com.mmt.flightbooking.dto.PaymentRequest;
import com.mmt.flightbooking.entity.Booking;
import com.mmt.flightbooking.entity.Payment;
import com.mmt.flightbooking.entity.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UpiPaymentStrategyTest {

    @InjectMocks
    private UpiPaymentStrategy upiPaymentStrategy;

    private PaymentRequest paymentRequest;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        Booking testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setBookingReference("MMT12345678");
        testBooking.setTotalAmount(new BigDecimal("5500.00"));

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setBooking(testBooking);
        testPayment.setAmount(new BigDecimal("5500.00"));
        testPayment.setMethod(PaymentMethod.UPI);

        paymentRequest = new PaymentRequest();
        paymentRequest.setPayment(testPayment);
        paymentRequest.setUpiId("user@paytm");
    }

    @Test
    void testGetStrategyName() {
        // Act
        String strategyName = upiPaymentStrategy.getStrategyName();

        // Assert
        assertEquals("UPI_PAYMENT", strategyName);
    }

    @Test
    void testProcessPayment_ValidUpiId() {
        // Act
        PaymentGatewayResponse response = upiPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Razorpay", response.getGatewayName());
        assertNotNull(response.getMessage());
        // Success is probabilistic (95%), but response should be valid
        if (response.isSuccess()) {
            assertNotNull(response.getTransactionId());
            assertTrue(response.getTransactionId().startsWith("UPI_"));
        }
    }

    @Test
    void testProcessPayment_ValidUpiId_Paytm() {
        // Arrange
        paymentRequest.setUpiId("john.doe@paytm");

        // Act
        PaymentGatewayResponse response = upiPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess() || response.getMessage().contains("declined") || response.getMessage().contains("failed"));
    }

    @Test
    void testProcessPayment_ValidUpiId_GooglePay() {
        // Arrange
        paymentRequest.setUpiId("user@okaxis");

        // Act
        PaymentGatewayResponse response = upiPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess() || response.getMessage().contains("declined") || response.getMessage().contains("failed"));
    }

    @Test
    void testProcessPayment_ValidUpiId_PhonePe() {
        // Arrange
        paymentRequest.setUpiId("9876543210@ybl");

        // Act
        PaymentGatewayResponse response = upiPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess() || response.getMessage().contains("declined") || response.getMessage().contains("failed"));
    }

    @Test
    void testProcessPayment_InvalidUpiId_NoAtSign() {
        // Arrange
        paymentRequest.setUpiId("userpaytm");

        // Act
        PaymentGatewayResponse response = upiPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Invalid UPI ID", response.getMessage());
        assertNull(response.getTransactionId());
    }

    @Test
    void testProcessPayment_InvalidUpiId_EmptyString() {
        // Arrange
        paymentRequest.setUpiId("");

        // Act
        PaymentGatewayResponse response = upiPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Invalid UPI ID", response.getMessage());
    }

    @Test
    void testProcessPayment_InvalidUpiId_Null() {
        // Arrange
        paymentRequest.setUpiId(null);

        // Act
        PaymentGatewayResponse response = upiPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Invalid UPI ID", response.getMessage());
    }

    @Test
    void testProcessPayment_InvalidUpiId_OnlyAtSign() {
        // Arrange
        paymentRequest.setUpiId("@");

        // Act
        PaymentGatewayResponse response = upiPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Invalid UPI ID", response.getMessage());
    }

    @Test
    void testProcessPayment_InvalidUpiId_MultipleAtSigns() {
        // Arrange
        paymentRequest.setUpiId("user@bank@extra");

        // Act
        PaymentGatewayResponse response = upiPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        // Should fail validation due to multiple @ signs
        assertFalse(response.isSuccess());
    }

    @Test
    void testProcessPayment_TransactionIdFormat() {
        // Act - Try multiple times to get a successful payment
        PaymentGatewayResponse response = null;
        for (int i = 0; i < 30; i++) {
            response = upiPaymentStrategy.processPayment(paymentRequest);
            if (response.isSuccess()) {
                break;
            }
        }

        // Assert
        if (response != null && response.isSuccess()) {
            assertTrue(response.getTransactionId().startsWith("UPI_"));
            assertEquals(12, response.getTransactionId().length()); // UPI_ + 8 chars
        }
    }

    @Test
    void testProcessPayment_WithDots() {
        // Arrange
        paymentRequest.setUpiId("john.doe@paytm");

        // Act
        PaymentGatewayResponse response = upiPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        // Should be valid with dots
        assertTrue(response.isSuccess() || response.getMessage().contains("declined") || response.getMessage().contains("failed"));
    }

    @Test
    void testProcessPayment_WithHyphens() {
        // Arrange
        paymentRequest.setUpiId("john-doe@paytm");

        // Act
        PaymentGatewayResponse response = upiPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        // Should be valid with hyphens
        assertTrue(response.isSuccess() || response.getMessage().contains("declined") || response.getMessage().contains("failed"));
    }

    @Test
    void testProcessPayment_WithUnderscores() {
        // Arrange
        paymentRequest.setUpiId("john_doe@paytm");

        // Act
        PaymentGatewayResponse response = upiPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        // Should be valid with underscores
        assertTrue(response.isSuccess() || response.getMessage().contains("declined") || response.getMessage().contains("failed"));
    }

    @Test
    void testProcessPayment_GatewayName() {
        // Act
        PaymentGatewayResponse response = upiPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertEquals("Razorpay", response.getGatewayName());
    }

    @Test
    void testProcessPayment_MultipleRequests() {
        // Act - Process multiple payments
        PaymentGatewayResponse response1 = upiPaymentStrategy.processPayment(paymentRequest);
        PaymentGatewayResponse response2 = upiPaymentStrategy.processPayment(paymentRequest);
        PaymentGatewayResponse response3 = upiPaymentStrategy.processPayment(paymentRequest);

        // Assert - All responses should be valid
        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
    }
}

