package com.mmt.flightbooking.service.payment;

import com.mmt.flightbooking.dto.PaymentRequest;
import com.mmt.flightbooking.entity.Booking;
import com.mmt.flightbooking.entity.Payment;
import com.mmt.flightbooking.entity.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

    @Mock
    private PaymentStrategyFactory strategyFactory;

    @Mock
    private PaymentStrategy mockStrategy;

    @InjectMocks
    private PaymentGatewayService paymentGatewayService;

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
    }

    @Test
    void testProcessPayment_Success() {
        // Arrange
        PaymentGatewayResponse expectedResponse = new PaymentGatewayResponse();
        expectedResponse.setSuccess(true);
        expectedResponse.setMessage("Payment successful");
        expectedResponse.setTransactionId("TXN_12345678");
        expectedResponse.setGatewayName("Razorpay");

        when(strategyFactory.isSupported(PaymentMethod.CREDIT_CARD)).thenReturn(true);
        when(strategyFactory.getStrategy(PaymentMethod.CREDIT_CARD)).thenReturn(mockStrategy);
        when(mockStrategy.processPayment(paymentRequest)).thenReturn(expectedResponse);
        when(mockStrategy.getStrategyName()).thenReturn("CARD_PAYMENT");

        // Act
        PaymentGatewayResponse response = paymentGatewayService.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Payment successful", response.getMessage());
        assertEquals("TXN_12345678", response.getTransactionId());
        assertEquals("Razorpay", response.getGatewayName());

        verify(strategyFactory, times(1)).isSupported(PaymentMethod.CREDIT_CARD);
        verify(strategyFactory, times(1)).getStrategy(PaymentMethod.CREDIT_CARD);
        verify(mockStrategy, times(1)).processPayment(paymentRequest);
    }

    @Test
    void testProcessPayment_UnsupportedMethod() {
        // Arrange
        when(strategyFactory.isSupported(PaymentMethod.CREDIT_CARD)).thenReturn(false);

        // Act
        PaymentGatewayResponse response = paymentGatewayService.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Unsupported payment method"));
        assertEquals("System", response.getGatewayName());
        assertNull(response.getTransactionId());

        verify(strategyFactory, times(1)).isSupported(PaymentMethod.CREDIT_CARD);
        verify(strategyFactory, never()).getStrategy(any());
        verify(mockStrategy, never()).processPayment(any());
    }

    @Test
    void testProcessPayment_StrategyThrowsException() {
        // Arrange
        when(strategyFactory.isSupported(PaymentMethod.CREDIT_CARD)).thenReturn(true);
        when(strategyFactory.getStrategy(PaymentMethod.CREDIT_CARD)).thenReturn(mockStrategy);
        when(mockStrategy.processPayment(paymentRequest))
            .thenThrow(new RuntimeException("Gateway connection failed"));

        // Act
        PaymentGatewayResponse response = paymentGatewayService.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Payment gateway error"));
        assertTrue(response.getMessage().contains("Gateway connection failed"));
    }

    @Test
    void testProcessPayment_UPIMethod() {
        // Arrange
        testPayment.setMethod(PaymentMethod.UPI);
        paymentRequest.setUpiId("user@paytm");

        PaymentGatewayResponse expectedResponse = new PaymentGatewayResponse();
        expectedResponse.setSuccess(true);
        expectedResponse.setTransactionId("UPI_12345678");

        when(strategyFactory.isSupported(PaymentMethod.UPI)).thenReturn(true);
        when(strategyFactory.getStrategy(PaymentMethod.UPI)).thenReturn(mockStrategy);
        when(mockStrategy.processPayment(paymentRequest)).thenReturn(expectedResponse);

        // Act
        PaymentGatewayResponse response = paymentGatewayService.processPayment(paymentRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("UPI_12345678", response.getTransactionId());
        verify(strategyFactory, times(1)).getStrategy(PaymentMethod.UPI);
    }

    @Test
    void testProcessPayment_NetBankingMethod() {
        // Arrange
        testPayment.setMethod(PaymentMethod.NET_BANKING);
        paymentRequest.setBankCode("HDFC");

        PaymentGatewayResponse expectedResponse = new PaymentGatewayResponse();
        expectedResponse.setSuccess(true);
        expectedResponse.setTransactionId("NB_12345678");

        when(strategyFactory.isSupported(PaymentMethod.NET_BANKING)).thenReturn(true);
        when(strategyFactory.getStrategy(PaymentMethod.NET_BANKING)).thenReturn(mockStrategy);
        when(mockStrategy.processPayment(paymentRequest)).thenReturn(expectedResponse);

        // Act
        PaymentGatewayResponse response = paymentGatewayService.processPayment(paymentRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("NB_12345678", response.getTransactionId());
        verify(strategyFactory, times(1)).getStrategy(PaymentMethod.NET_BANKING);
    }

    @Test
    void testProcessPayment_WalletMethod() {
        // Arrange
        testPayment.setMethod(PaymentMethod.WALLET);
        paymentRequest.setWalletType("PAYTM");

        PaymentGatewayResponse expectedResponse = new PaymentGatewayResponse();
        expectedResponse.setSuccess(true);
        expectedResponse.setTransactionId("WALLET_12345678");

        when(strategyFactory.isSupported(PaymentMethod.WALLET)).thenReturn(true);
        when(strategyFactory.getStrategy(PaymentMethod.WALLET)).thenReturn(mockStrategy);
        when(mockStrategy.processPayment(paymentRequest)).thenReturn(expectedResponse);

        // Act
        PaymentGatewayResponse response = paymentGatewayService.processPayment(paymentRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("WALLET_12345678", response.getTransactionId());
        verify(strategyFactory, times(1)).getStrategy(PaymentMethod.WALLET);
    }

    @Test
    void testProcessPayment_PaymentFailed() {
        // Arrange
        PaymentGatewayResponse expectedResponse = new PaymentGatewayResponse();
        expectedResponse.setSuccess(false);
        expectedResponse.setMessage("Insufficient funds");
        expectedResponse.setGatewayName("Razorpay");

        when(strategyFactory.isSupported(PaymentMethod.CREDIT_CARD)).thenReturn(true);
        when(strategyFactory.getStrategy(PaymentMethod.CREDIT_CARD)).thenReturn(mockStrategy);
        when(mockStrategy.processPayment(paymentRequest)).thenReturn(expectedResponse);

        // Act
        PaymentGatewayResponse response = paymentGatewayService.processPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Insufficient funds", response.getMessage());
        assertNull(response.getTransactionId());
    }

    @Test
    void testProcessPayment_StrategyReturnsNull() {
        // Arrange
        when(strategyFactory.isSupported(PaymentMethod.CREDIT_CARD)).thenReturn(true);
        when(strategyFactory.getStrategy(PaymentMethod.CREDIT_CARD)).thenReturn(mockStrategy);
        when(mockStrategy.processPayment(paymentRequest)).thenReturn(null);

        // Act
        PaymentGatewayResponse response = paymentGatewayService.processPayment(paymentRequest);

        // Assert - The service catches the exception and returns error response
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Payment gateway error"));
    }

    @Test
    void testProcessPayment_GetStrategyThrowsException() {
        // Arrange
        when(strategyFactory.isSupported(PaymentMethod.CREDIT_CARD)).thenReturn(true);
        when(strategyFactory.getStrategy(PaymentMethod.CREDIT_CARD))
            .thenThrow(new IllegalArgumentException("Strategy not found"));

        // Act
        PaymentGatewayResponse response = paymentGatewayService.processPayment(paymentRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Payment gateway error"));
    }

    @Test
    void testProcessPayment_AllPaymentMethods() {
        // Test that service can handle all payment methods
        PaymentMethod[] methods = {
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.DEBIT_CARD,
            PaymentMethod.UPI,
            PaymentMethod.NET_BANKING,
            PaymentMethod.WALLET
        };

        for (PaymentMethod method : methods) {
            testPayment.setMethod(method);
            
            PaymentGatewayResponse mockResponse = new PaymentGatewayResponse();
            mockResponse.setSuccess(true);
            mockResponse.setTransactionId("TXN_" + method.name());

            when(strategyFactory.isSupported(method)).thenReturn(true);
            when(strategyFactory.getStrategy(method)).thenReturn(mockStrategy);
            when(mockStrategy.processPayment(any())).thenReturn(mockResponse);

            PaymentGatewayResponse response = paymentGatewayService.processPayment(paymentRequest);
            
            assertNotNull(response);
            assertTrue(response.isSuccess());
        }
    }
}

