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
class CardPaymentStrategyTest {

    @InjectMocks
    private CardPaymentStrategy cardPaymentStrategy;

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
        testPayment.setMethod(PaymentMethod.CREDIT_CARD);

        paymentRequest = new PaymentRequest();
        paymentRequest.setPayment(testPayment);
        paymentRequest.setCardNumber("4111111111111111");
        paymentRequest.setCardHolderName("John Doe");
        paymentRequest.setExpiryMonth("12");
        paymentRequest.setExpiryYear("2025");
        paymentRequest.setCvv("123");
    }

    @Test
    void testGetStrategyName() {
        // Act
        String strategyName = cardPaymentStrategy.getStrategyName();

        // Assert
        assertEquals("CARD_PAYMENT", strategyName);
    }

    @Test
    void testProcessPayment_ValidCard() {
        // Act
        PaymentGatewayResponse response = cardPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Razorpay", response.getGatewayName());
        assertNotNull(response.getMessage());
        // Success is probabilistic, but response should be valid
        if (response.isSuccess()) {
            assertNotNull(response.getTransactionId());
            assertTrue(response.getTransactionId().startsWith("TXN_"));
        }
    }

    @Test
    void testProcessPayment_InvalidCard_TooShort() {
        // Arrange
        paymentRequest.setCardNumber("123456789012"); // Only 12 digits

        // Act
        PaymentGatewayResponse response = cardPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Invalid card details", response.getMessage());
        assertNull(response.getTransactionId());
    }

    @Test
    void testProcessPayment_InvalidCard_TooLong() {
        // Arrange
        paymentRequest.setCardNumber("12345678901234567890"); // 20 digits

        // Act
        PaymentGatewayResponse response = cardPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Invalid card details", response.getMessage());
    }

    @Test
    void testProcessPayment_NullCardNumber() {
        // Arrange
        paymentRequest.setCardNumber(null);

        // Act
        PaymentGatewayResponse response = cardPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Invalid card details", response.getMessage());
    }

    @Test
    void testProcessPayment_EmptyCardNumber() {
        // Arrange
        paymentRequest.setCardNumber("");

        // Act
        PaymentGatewayResponse response = cardPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Invalid card details", response.getMessage());
    }

    @Test
    void testProcessPayment_ValidVisaCard() {
        // Arrange
        paymentRequest.setCardNumber("4532015112830366"); // Valid Visa format

        // Act
        PaymentGatewayResponse response = cardPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getMessage());
    }

    @Test
    void testProcessPayment_ValidMasterCard() {
        // Arrange
        paymentRequest.setCardNumber("5425233430109903"); // Valid MasterCard format

        // Act
        PaymentGatewayResponse response = cardPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getMessage());
    }

    @Test
    void testProcessPayment_ValidAmexCard() {
        // Arrange
        paymentRequest.setCardNumber("378282246310005"); // Valid Amex format (15 digits)

        // Act
        PaymentGatewayResponse response = cardPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getMessage());
    }

    @Test
    void testProcessPayment_MinimumValidLength() {
        // Arrange
        paymentRequest.setCardNumber("1234567890123"); // 13 digits (minimum valid)

        // Act
        PaymentGatewayResponse response = cardPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        // Should not fail validation
        assertTrue(response.isSuccess() || response.getMessage().contains("declined"));
    }

    @Test
    void testProcessPayment_MaximumValidLength() {
        // Arrange
        paymentRequest.setCardNumber("1234567890123456789"); // 19 digits (maximum valid)

        // Act
        PaymentGatewayResponse response = cardPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        // Should not fail validation
        assertTrue(response.isSuccess() || response.getMessage().contains("declined"));
    }

    @Test
    void testProcessPayment_TransactionIdFormat() {
        // Act - Try multiple times to get a successful payment
        PaymentGatewayResponse response = null;
        for (int i = 0; i < 20; i++) {
            response = cardPaymentStrategy.processPayment(paymentRequest);
            if (response.isSuccess()) {
                break;
            }
        }

        // Assert - If we got a successful payment, verify transaction ID format
        if (response != null && response.isSuccess()) {
            assertTrue(response.getTransactionId().startsWith("TXN_"));
            assertEquals(12, response.getTransactionId().length()); // TXN_ + 8 chars
        }
    }

    @Test
    void testProcessPayment_GatewayName() {
        // Act
        PaymentGatewayResponse response = cardPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertEquals("Razorpay", response.getGatewayName());
    }

    @Test
    void testProcessPayment_MultipleRequests() {
        // Act - Process multiple payments
        PaymentGatewayResponse response1 = cardPaymentStrategy.processPayment(paymentRequest);
        PaymentGatewayResponse response2 = cardPaymentStrategy.processPayment(paymentRequest);
        PaymentGatewayResponse response3 = cardPaymentStrategy.processPayment(paymentRequest);

        // Assert - All responses should be valid
        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
    }

    @Test
    void testCardMasking() {
        // This tests the internal masking logic indirectly through logging
        // We verify the strategy doesn't throw errors with various card formats
        
        paymentRequest.setCardNumber("4111111111111111");
        assertDoesNotThrow(() -> cardPaymentStrategy.processPayment(paymentRequest));

        paymentRequest.setCardNumber("123");
        assertDoesNotThrow(() -> cardPaymentStrategy.processPayment(paymentRequest));
    }
}

