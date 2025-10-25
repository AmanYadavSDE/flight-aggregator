package com.mmt.flightbooking.controller;

import com.mmt.flightbooking.dto.PaymentRequest;
import com.mmt.flightbooking.dto.PaymentResponse;
import com.mmt.flightbooking.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments")
@Tag(name = "Payment Processing", description = "Payment processing APIs")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping("/process")
    @Operation(summary = "Process payment", description = "Process payment for a booking")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        logger.info("Processing payment for booking: {}", 
                   request.getPayment().getBooking().getBookingReference());
        
        try {
            var result = paymentService.processPayment(request);
            
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(result.isSuccess());
            response.setMessage(result.getMessage());
            response.setTransactionId(result.getTransactionId());
            response.setStatus(result.isSuccess() ? "COMPLETED" : "FAILED");
            
            if (result.isSuccess()) {
                logger.info("Payment processed successfully: {}", result.getTransactionId());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Payment processing failed: {}", result.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Payment processing error", e);
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Payment processing failed: " + e.getMessage());
            errorResponse.setStatus("FAILED");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/{transactionId}/status")
    @Operation(summary = "Get payment status", description = "Get the status of a payment transaction")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String transactionId) {
        logger.info("Getting payment status for transaction: {}", transactionId);
        
        try {
            var payment = paymentService.getPaymentByTransactionId(transactionId);
            
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(payment.getStatus().toString().equals("COMPLETED"));
            response.setTransactionId(payment.getTransactionId());
            response.setStatus(payment.getStatus().toString());
            response.setMessage("Payment status retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting payment status", e);
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Failed to get payment status: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get payment by booking", description = "Get payment details for a specific booking")
    public ResponseEntity<PaymentResponse> getPaymentByBooking(@PathVariable Long bookingId) {
        logger.info("Getting payment for booking: {}", bookingId);
        
        try {
            var payment = paymentService.getPaymentByBookingId(bookingId);
            
            PaymentResponse response = new PaymentResponse();
            response.setSuccess(payment.getStatus().toString().equals("COMPLETED"));
            response.setTransactionId(payment.getTransactionId());
            response.setStatus(payment.getStatus().toString());
            response.setMessage("Payment details retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting payment by booking", e);
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Failed to get payment details: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
