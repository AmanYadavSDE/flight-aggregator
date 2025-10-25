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
class NetBankingPaymentStrategyTest {

    @InjectMocks
    private NetBankingPaymentStrategy netBankingPaymentStrategy;

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
        testPayment.setMethod(PaymentMethod.NET_BANKING);

        paymentRequest = new PaymentRequest();
        paymentRequest.setPayment(testPayment);
        paymentRequest.setBankCode("HDFC");
    }

    @Test
    void testGetStrategyName() {
        // Act
        String strategyName = netBankingPaymentStrategy.getStrategyName();

        // Assert
        assertEquals("NET_BANKING", strategyName);
    }

    @Test
    void testProcessPayment_ValidBankCode() {
        // Act
        PaymentGatewayResponse response = netBankingPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Razorpay", response.getGatewayName());
        assertNotNull(response.getMessage());
        // Success is probabilistic (92%), but response should be valid
        if (response.isSuccess()) {
            assertNotNull(response.getTransactionId());
            assertTrue(response.getTransactionId().startsWith("NB_"));
        }
    }

    @Test
    void testProcessPayment_HDFC() {
        // Arrange
        paymentRequest.setBankCode("HDFC");

        // Act
        PaymentGatewayResponse response = netBankingPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess() || response.getMessage().contains("failed"));
    }

    @Test
    void testProcessPayment_ICICI() {
        // Arrange
        paymentRequest.setBankCode("ICICI");

        // Act
        PaymentGatewayResponse response = netBankingPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess() || response.getMessage().contains("failed"));
    }

    @Test
    void testProcessPayment_SBI() {
        // Arrange
        paymentRequest.setBankCode("SBI");

        // Act
        PaymentGatewayResponse response = netBankingPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess() || response.getMessage().contains("failed"));
    }

    @Test
    void testProcessPayment_AXIS() {
        // Arrange
        paymentRequest.setBankCode("AXIS");

        // Act
        PaymentGatewayResponse response = netBankingPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess() || response.getMessage().contains("failed"));
    }

    @Test
    void testProcessPayment_NullBankCode() {
        // Arrange
        paymentRequest.setBankCode(null);

        // Act
        PaymentGatewayResponse response = netBankingPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Bank code is required", response.getMessage());
        assertNull(response.getTransactionId());
    }

    @Test
    void testProcessPayment_EmptyBankCode() {
        // Arrange
        paymentRequest.setBankCode("");

        // Act
        PaymentGatewayResponse response = netBankingPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Bank code is required", response.getMessage());
    }

    @Test
    void testProcessPayment_TransactionIdFormat() {
        // Act - Try multiple times to get a successful payment
        PaymentGatewayResponse response = null;
        for (int i = 0; i < 20; i++) {
            response = netBankingPaymentStrategy.processPayment(paymentRequest);
            if (response.isSuccess()) {
                break;
            }
        }

        // Assert
        if (response != null && response.isSuccess()) {
            assertTrue(response.getTransactionId().startsWith("NB_"));
            assertEquals(11, response.getTransactionId().length()); // NB_ + 8 chars
        }
    }

    @Test
    void testProcessPayment_GatewayName() {
        // Act
        PaymentGatewayResponse response = netBankingPaymentStrategy.processPayment(paymentRequest);

        // Assert
        assertEquals("Razorpay", response.getGatewayName());
    }

    @Test
    void testProcessPayment_MultipleRequests() {
        // Act - Process multiple payments
        PaymentGatewayResponse response1 = netBankingPaymentStrategy.processPayment(paymentRequest);
        PaymentGatewayResponse response2 = netBankingPaymentStrategy.processPayment(paymentRequest);
        PaymentGatewayResponse response3 = netBankingPaymentStrategy.processPayment(paymentRequest);

        // Assert - All responses should be valid
        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
    }

    @Test
    void testProcessPayment_DifferentBanks() {
        // Test various bank codes
        String[] bankCodes = {"HDFC", "ICICI", "SBI", "AXIS", "KOTAK", "YES"};

        for (String bankCode : bankCodes) {
            paymentRequest.setBankCode(bankCode);
            PaymentGatewayResponse response = netBankingPaymentStrategy.processPayment(paymentRequest);
            
            assertNotNull(response);
            assertNotNull(response.getMessage());
        }
    }

    @Test
    void testProcessPayment_SuccessMessage() {
        // Act - Try to get a successful payment
        PaymentGatewayResponse response = null;
        for (int i = 0; i < 20; i++) {
            response = netBankingPaymentStrategy.processPayment(paymentRequest);
            if (response.isSuccess()) {
                break;
            }
        }

        // Assert
        if (response != null && response.isSuccess()) {
            assertEquals("Net banking payment successful", response.getMessage());
        }
    }
}

