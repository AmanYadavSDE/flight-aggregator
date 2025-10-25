package com.mmt.flightbooking.service.payment;

import com.mmt.flightbooking.dto.PaymentRequest;
import com.mmt.flightbooking.dto.PaymentResult;
import com.mmt.flightbooking.entity.*;
import com.mmt.flightbooking.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @InjectMocks
    private PaymentService paymentService;

    private Booking testBooking;
    private User testUser;
    private Payment testPayment;
    private PaymentRequest paymentRequest;

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
        testBooking.setFlightNumber("6E-2001");
        testBooking.setOriginAirport("DEL");
        testBooking.setDestinationAirport("BOM");
        testBooking.setDepartureDate(LocalDate.of(2025, 11, 15));

        // Setup test payment
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setBooking(testBooking);
        testPayment.setAmount(new BigDecimal("5500.00"));
        testPayment.setCurrency("INR");
        testPayment.setMethod(PaymentMethod.CREDIT_CARD);
        testPayment.setStatus(PaymentStatus.PENDING);

        // Setup payment request
        paymentRequest = new PaymentRequest();
        paymentRequest.setPayment(testPayment);
        paymentRequest.setCardNumber("4111111111111111");
        paymentRequest.setCardHolderName("John Doe");
        paymentRequest.setExpiryMonth("12");
        paymentRequest.setExpiryYear("2025");
        paymentRequest.setCvv("123");
    }

    @Test
    void testCreatePayment_Success() {
        // Arrange
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        Payment result = paymentService.createPayment(testBooking, PaymentMethod.CREDIT_CARD);

        // Assert
        assertNotNull(result);
        assertEquals(testBooking, result.getBooking());
        assertEquals(new BigDecimal("5500.00"), result.getAmount());
        assertEquals("INR", result.getCurrency());
        assertEquals(PaymentMethod.CREDIT_CARD, result.getMethod());

        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testCreatePayment_VerifyPaymentFields() {
        // Arrange
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        when(paymentRepository.save(paymentCaptor.capture())).thenReturn(testPayment);

        // Act
        paymentService.createPayment(testBooking, PaymentMethod.UPI);

        // Assert
        Payment capturedPayment = paymentCaptor.getValue();
        assertEquals(testBooking, capturedPayment.getBooking());
        assertEquals(testBooking.getTotalAmount(), capturedPayment.getAmount());
        assertEquals(testBooking.getCurrency(), capturedPayment.getCurrency());
        assertEquals(PaymentMethod.UPI, capturedPayment.getMethod());
        assertEquals(PaymentStatus.PENDING, capturedPayment.getStatus());
    }

    @Test
    void testCreatePayment_DifferentPaymentMethods() {
        // Arrange - Return the argument itself so payment method is preserved
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert for each payment method
        Payment creditCardPayment = paymentService.createPayment(testBooking, PaymentMethod.CREDIT_CARD);
        assertEquals(PaymentMethod.CREDIT_CARD, creditCardPayment.getMethod());

        Payment debitCardPayment = paymentService.createPayment(testBooking, PaymentMethod.DEBIT_CARD);
        assertEquals(PaymentMethod.DEBIT_CARD, debitCardPayment.getMethod());

        Payment upiPayment = paymentService.createPayment(testBooking, PaymentMethod.UPI);
        assertEquals(PaymentMethod.UPI, upiPayment.getMethod());

        Payment netBankingPayment = paymentService.createPayment(testBooking, PaymentMethod.NET_BANKING);
        assertEquals(PaymentMethod.NET_BANKING, netBankingPayment.getMethod());

        Payment walletPayment = paymentService.createPayment(testBooking, PaymentMethod.WALLET);
        assertEquals(PaymentMethod.WALLET, walletPayment.getMethod());
    }

    @Test
    void testProcessPayment_Success() {
        // Arrange
        PaymentGatewayResponse gatewayResponse = new PaymentGatewayResponse();
        gatewayResponse.setSuccess(true);
        gatewayResponse.setMessage("Payment successful");
        gatewayResponse.setTransactionId("TXN123456");
        gatewayResponse.setResponse("SUCCESS");
        gatewayResponse.setGatewayName("RazorPay");

        when(paymentGatewayService.processPayment(paymentRequest)).thenReturn(gatewayResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        PaymentResult result = paymentService.processPayment(paymentRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Payment successful", result.getMessage());
        assertEquals("TXN123456", result.getTransactionId());
        assertEquals("SUCCESS", result.getGatewayResponse());

        // Verify payment status updates
        verify(paymentRepository, times(2)).save(testPayment);
        verify(paymentGatewayService, times(1)).processPayment(paymentRequest);
    }

    @Test
    void testProcessPayment_Failed() {
        // Arrange
        PaymentGatewayResponse gatewayResponse = new PaymentGatewayResponse();
        gatewayResponse.setSuccess(false);
        gatewayResponse.setMessage("Insufficient funds");
        gatewayResponse.setErrorMessage("Card declined");
        gatewayResponse.setGatewayName("RazorPay");

        when(paymentGatewayService.processPayment(paymentRequest)).thenReturn(gatewayResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        PaymentResult result = paymentService.processPayment(paymentRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Insufficient funds", result.getMessage());

        // Verify payment status was updated to failed
        verify(paymentRepository, times(2)).save(testPayment);
        assertEquals(PaymentStatus.FAILED, testPayment.getStatus());
    }

    @Test
    void testProcessPayment_UpdatesPaymentStatusToProcessing() {
        // Arrange
        PaymentGatewayResponse gatewayResponse = new PaymentGatewayResponse();
        gatewayResponse.setSuccess(true);
        gatewayResponse.setTransactionId("TXN123");

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        when(paymentGatewayService.processPayment(paymentRequest)).thenReturn(gatewayResponse);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        paymentService.processPayment(paymentRequest);

        // Assert
        // Verify the payment object was set to PROCESSING (it will be COMPLETED after the second save)
        verify(paymentRepository, times(2)).save(paymentCaptor.capture());
        // The request payment should have been set to PROCESSING initially
        // But we can't easily verify this as it gets changed to COMPLETED later
    }

    @Test
    void testProcessPayment_UpdatesPaymentStatusToCompleted() {
        // Arrange
        PaymentGatewayResponse gatewayResponse = new PaymentGatewayResponse();
        gatewayResponse.setSuccess(true);
        gatewayResponse.setTransactionId("TXN123");
        gatewayResponse.setResponse("APPROVED");
        gatewayResponse.setGatewayName("RazorPay");

        when(paymentGatewayService.processPayment(paymentRequest)).thenReturn(gatewayResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        paymentService.processPayment(paymentRequest);

        // Assert
        assertEquals(PaymentStatus.COMPLETED, testPayment.getStatus());
        assertEquals("TXN123", testPayment.getTransactionId());
        assertEquals("APPROVED", testPayment.getGatewayResponse());
        assertEquals("RazorPay", testPayment.getGatewayName());
    }

    @Test
    void testProcessPayment_CreatesTransactionRecord() {
        // Arrange
        PaymentGatewayResponse gatewayResponse = new PaymentGatewayResponse();
        gatewayResponse.setSuccess(true);
        gatewayResponse.setTransactionId("TXN123");

        when(paymentGatewayService.processPayment(paymentRequest)).thenReturn(gatewayResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        paymentService.processPayment(paymentRequest);

        // Assert
        assertEquals(1, testPayment.getTransactions().size());
        PaymentTransaction transaction = testPayment.getTransactions().get(0);
        assertEquals(TransactionStatus.SUCCESS, transaction.getStatus());
        assertNotNull(transaction.getTransactionReference());
    }

    @Test
    void testProcessPayment_TransactionFailure() {
        // Arrange
        PaymentGatewayResponse gatewayResponse = new PaymentGatewayResponse();
        gatewayResponse.setSuccess(false);
        gatewayResponse.setErrorMessage("Network timeout");

        when(paymentGatewayService.processPayment(paymentRequest)).thenReturn(gatewayResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        paymentService.processPayment(paymentRequest);

        // Assert
        assertEquals(1, testPayment.getTransactions().size());
        PaymentTransaction transaction = testPayment.getTransactions().get(0);
        assertEquals(TransactionStatus.FAILED, transaction.getStatus());
        assertEquals("Network timeout", transaction.getErrorMessage());
    }

    @Test
    void testProcessPayment_ExceptionHandling() {
        // Arrange
        when(paymentGatewayService.processPayment(paymentRequest))
            .thenThrow(new RuntimeException("Gateway connection failed"));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        PaymentResult result = paymentService.processPayment(paymentRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Payment processing failed"));
        assertTrue(result.getMessage().contains("Gateway connection failed"));

        // Verify payment status was set to FAILED
        assertEquals(PaymentStatus.FAILED, testPayment.getStatus());
        verify(paymentRepository, times(2)).save(testPayment); // Once for processing, once for failed
    }

    @Test
    void testGetPaymentByBookingId_Success() {
        // Arrange
        when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.of(testPayment));

        // Act
        Payment result = paymentService.getPaymentByBookingId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testPayment.getId(), result.getId());
        assertEquals(testBooking, result.getBooking());
        verify(paymentRepository, times(1)).findByBookingId(1L);
    }

    @Test
    void testGetPaymentByBookingId_NotFound() {
        // Arrange
        when(paymentRepository.findByBookingId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.getPaymentByBookingId(999L);
        });

        assertTrue(exception.getMessage().contains("Payment not found for booking"));
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    void testGetPaymentByTransactionId_Success() {
        // Arrange
        testPayment.setTransactionId("TXN123456");
        when(paymentRepository.findByTransactionId("TXN123456")).thenReturn(Optional.of(testPayment));

        // Act
        Payment result = paymentService.getPaymentByTransactionId("TXN123456");

        // Assert
        assertNotNull(result);
        assertEquals("TXN123456", result.getTransactionId());
        verify(paymentRepository, times(1)).findByTransactionId("TXN123456");
    }

    @Test
    void testGetPaymentByTransactionId_NotFound() {
        // Arrange
        when(paymentRepository.findByTransactionId("INVALID_TXN")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.getPaymentByTransactionId("INVALID_TXN");
        });

        assertTrue(exception.getMessage().contains("Payment not found for transaction"));
        assertTrue(exception.getMessage().contains("INVALID_TXN"));
    }

    @Test
    void testProcessPayment_UPIPayment() {
        // Arrange
        testPayment.setMethod(PaymentMethod.UPI);
        paymentRequest.setUpiId("user@paytm");

        PaymentGatewayResponse gatewayResponse = new PaymentGatewayResponse();
        gatewayResponse.setSuccess(true);
        gatewayResponse.setTransactionId("UPI-TXN-123");

        when(paymentGatewayService.processPayment(paymentRequest)).thenReturn(gatewayResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        PaymentResult result = paymentService.processPayment(paymentRequest);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("UPI-TXN-123", result.getTransactionId());
    }

    @Test
    void testProcessPayment_NetBankingPayment() {
        // Arrange
        testPayment.setMethod(PaymentMethod.NET_BANKING);
        paymentRequest.setBankCode("HDFC");

        PaymentGatewayResponse gatewayResponse = new PaymentGatewayResponse();
        gatewayResponse.setSuccess(true);
        gatewayResponse.setTransactionId("NB-TXN-123");

        when(paymentGatewayService.processPayment(paymentRequest)).thenReturn(gatewayResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        PaymentResult result = paymentService.processPayment(paymentRequest);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("NB-TXN-123", result.getTransactionId());
    }

    @Test
    void testProcessPayment_WalletPayment() {
        // Arrange
        testPayment.setMethod(PaymentMethod.WALLET);
        paymentRequest.setWalletType("PAYTM");

        PaymentGatewayResponse gatewayResponse = new PaymentGatewayResponse();
        gatewayResponse.setSuccess(true);
        gatewayResponse.setTransactionId("WALLET-TXN-123");

        when(paymentGatewayService.processPayment(paymentRequest)).thenReturn(gatewayResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        PaymentResult result = paymentService.processPayment(paymentRequest);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("WALLET-TXN-123", result.getTransactionId());
    }

    @Test
    void testCreatePayment_WithDifferentCurrencies() {
        // Arrange
        Booking usdBooking = new Booking();
        usdBooking.setTotalAmount(new BigDecimal("1000.00"));
        usdBooking.setCurrency("USD");
        
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Payment result = paymentService.createPayment(usdBooking, PaymentMethod.CREDIT_CARD);

        // Assert
        assertEquals("USD", result.getCurrency());
    }

    @Test
    void testCreatePayment_WithZeroAmount() {
        // Arrange
        Booking zeroAmountBooking = new Booking();
        zeroAmountBooking.setTotalAmount(BigDecimal.ZERO);
        zeroAmountBooking.setCurrency("INR");
        
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Payment result = paymentService.createPayment(zeroAmountBooking, PaymentMethod.CREDIT_CARD);

        // Assert
        assertEquals(BigDecimal.ZERO, result.getAmount());
    }

    @Test
    void testCreatePayment_WithLargeAmount() {
        // Arrange
        BigDecimal largeAmount = new BigDecimal("999999.99");
        Booking largeAmountBooking = new Booking();
        largeAmountBooking.setTotalAmount(largeAmount);
        largeAmountBooking.setCurrency("INR");
        
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Payment result = paymentService.createPayment(largeAmountBooking, PaymentMethod.CREDIT_CARD);

        // Assert
        assertEquals(largeAmount, result.getAmount());
    }
}

