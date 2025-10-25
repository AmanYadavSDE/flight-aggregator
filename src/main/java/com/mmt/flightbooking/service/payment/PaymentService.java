package com.mmt.flightbooking.service.payment;

import com.mmt.flightbooking.dto.PaymentRequest;
import com.mmt.flightbooking.dto.PaymentResult;
import com.mmt.flightbooking.entity.*;
import com.mmt.flightbooking.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PaymentGatewayService paymentGatewayService;
    
    @Transactional
    public Payment createPayment(Booking booking, PaymentMethod method) {
        logger.info("Creating payment for booking: {} with method: {}", 
                   booking.getBookingReference(), method);
        
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setCurrency(booking.getCurrency());
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.PENDING);
        
        return paymentRepository.save(payment);
    }
    
    @Transactional
    public PaymentResult processPayment(PaymentRequest request) {
        logger.info("Processing payment for booking: {}", 
                   request.getPayment().getBooking().getBookingReference());
        
        try {
            // Update payment status to processing
            request.getPayment().setStatus(PaymentStatus.PROCESSING);
            paymentRepository.save(request.getPayment());
            
            // Create transaction record
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setPayment(request.getPayment());
            transaction.setStatus(TransactionStatus.INITIATED);
            transaction.setTransactionReference(UUID.randomUUID().toString());
            
            // Call appropriate payment gateway based on method
            PaymentGatewayResponse response = paymentGatewayService.processPayment(request);
            
            // Update transaction status
            if (response.isSuccess()) {
                transaction.setStatus(TransactionStatus.SUCCESS);
                request.getPayment().setStatus(PaymentStatus.COMPLETED);
                request.getPayment().setTransactionId(response.getTransactionId());
                request.getPayment().setGatewayResponse(response.getResponse());
                request.getPayment().setGatewayName(response.getGatewayName());
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                request.getPayment().setStatus(PaymentStatus.FAILED);
                transaction.setErrorMessage(response.getErrorMessage());
            }
            
            // Save transaction
            request.getPayment().getTransactions().add(transaction);
            paymentRepository.save(request.getPayment());
            
            PaymentResult result = new PaymentResult();
            result.setSuccess(response.isSuccess());
            result.setMessage(response.getMessage());
            result.setTransactionId(response.getTransactionId());
            result.setGatewayResponse(response.getResponse());
            
            logger.info("Payment processed: success={}, transactionId={}", 
                       response.isSuccess(), response.getTransactionId());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Payment processing failed", e);
            
            // Update payment status to failed
            request.getPayment().setStatus(PaymentStatus.FAILED);
            paymentRepository.save(request.getPayment());
            
            return new PaymentResult(false, "Payment processing failed: " + e.getMessage());
        }
    }
    
    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
            .orElseThrow(() -> new RuntimeException("Payment not found for booking: " + bookingId));
    }
    
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new RuntimeException("Payment not found for transaction: " + transactionId));
    }
}
