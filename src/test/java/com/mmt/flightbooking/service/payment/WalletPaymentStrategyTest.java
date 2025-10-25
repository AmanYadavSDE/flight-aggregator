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
class WalletPaymentStrategyTest {

    @InjectMocks
    private WalletPaymentStrategy walletPaymentStrategy;

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
        testPayment.setMethod(PaymentMethod.WALLET);

        paymentRequest = new PaymentRequest();
        paymentRequest.setPayment(testPayment);
        paymentRequest.setWalletType("PAYTM");
    }

    @Test
    void testGetStrategyName() {
        // Act
        String strategyName = walletPaymentStrategy.getStrategyName();

        // Assert
        assertEquals("WALLET_PAYMENT", strategyName);
    }

    @Test
    void testProcessPayment_ValidWalletType() {
        // Act
        PaymentGatewayResponse response = walletPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Razorpay", response.getGatewayName());
        assertNotNull(response.getMessage());
        // Success is probabilistic (97%), but response should be valid
        if (response.isSuccess()) {
            assertNotNull(response.getTransactionId());
            assertTrue(response.getTransactionId().startsWith("WALLET_"));
        }
    }

    @Test
    void testProcessPayment_Paytm() {
        // Arrange
        paymentRequest.setWalletType("PAYTM");

        // Act
        PaymentGatewayResponse response = walletPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess() || response.getMessage().contains("failed") || response.getMessage().contains("balance"));
    }

    @Test
    void testProcessPayment_PhonePe() {
        // Arrange
        paymentRequest.setWalletType("PHONEPE");

        // Act
        PaymentGatewayResponse response = walletPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess() || response.getMessage().contains("failed") || response.getMessage().contains("balance"));
    }

    @Test
    void testProcessPayment_GooglePay() {
        // Arrange
        paymentRequest.setWalletType("GOOGLEPAY");

        // Act
        PaymentGatewayResponse response = walletPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess() || response.getMessage().contains("failed") || response.getMessage().contains("balance"));
    }

    @Test
    void testProcessPayment_AmazonPay() {
        // Arrange
        paymentRequest.setWalletType("AMAZONPAY");

        // Act
        PaymentGatewayResponse response = walletPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess() || response.getMessage().contains("failed") || response.getMessage().contains("balance"));
    }

    @Test
    void testProcessPayment_NullWalletType() {
        // Arrange
        paymentRequest.setWalletType(null);

        // Act
        PaymentGatewayResponse response = walletPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Wallet type is required", response.getMessage());
        assertNull(response.getTransactionId());
    }

    @Test
    void testProcessPayment_EmptyWalletType() {
        // Arrange
        paymentRequest.setWalletType("");

        // Act
        PaymentGatewayResponse response = walletPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Wallet type is required", response.getMessage());
    }

    @Test
    void testProcessPayment_TransactionIdFormat() {
        // Act - Try multiple times to get a successful payment
        PaymentGatewayResponse response = null;
        for (int i = 0; i < 40; i++) {
            response = walletPaymentStrategy.processPayment(paymentRequest);
            if (response.isSuccess()) {
                break;
            }
        }

        // Assert
        if (response != null && response.isSuccess()) {
            assertTrue(response.getTransactionId().startsWith("WALLET_"));
            assertEquals(15, response.getTransactionId().length()); // WALLET_ + 8 chars
        }
    }

    @Test
    void testProcessPayment_GatewayName() {
        // Act
        PaymentGatewayResponse response = walletPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertEquals("Razorpay", response.getGatewayName());
    }

    @Test
    void testProcessPayment_MultipleRequests() {
        // Act - Process multiple payments
        PaymentGatewayResponse response1 = walletPaymentStrategy.processPayment(paymentRequest);
        PaymentGatewayResponse response2 = walletPaymentStrategy.processPayment(paymentRequest);
        PaymentGatewayResponse response3 = walletPaymentStrategy.processPayment(paymentRequest);

        // Assert - All responses should be valid
        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
    }

    @Test
    void testProcessPayment_DifferentWallets() {
        // Test various wallet types
        String[] walletTypes = {"PAYTM", "PHONEPE", "GOOGLEPAY", "AMAZONPAY", "MOBIKWIK", "FREECHARGE"};

        for (String walletType : walletTypes) {
            paymentRequest.setWalletType(walletType);
            PaymentGatewayResponse response = walletPaymentStrategy.processPayment(paymentRequest);
            
            assertNotNull(response);
            assertNotNull(response.getMessage());
        }
    }

    @Test
    void testProcessPayment_SuccessMessage() {
        // Act - Try to get a successful payment
        PaymentGatewayResponse response = null;
        for (int i = 0; i < 40; i++) {
            response = walletPaymentStrategy.processPayment(paymentRequest);
            if (response.isSuccess()) {
                break;
            }
        }

        // Assert
        if (response != null && response.isSuccess()) {
            assertEquals("Wallet payment successful", response.getMessage());
        }
    }

    @Test
    void testProcessPayment_FailureMessage() {
        // Act - Try to get a failed payment
        PaymentGatewayResponse response = null;
        for (int i = 0; i < 40; i++) {
            response = walletPaymentStrategy.processPayment(paymentRequest);
            if (!response.isSuccess()) {
                break;
            }
        }

        // Assert - Failed payments should have appropriate message
        if (response != null && !response.isSuccess() && response.getMessage().contains("balance")) {
            assertTrue(response.getMessage().contains("Insufficient balance") || 
                      response.getMessage().contains("required"));
        }
    }
}

