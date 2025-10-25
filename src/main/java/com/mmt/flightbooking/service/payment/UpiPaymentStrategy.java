package com.mmt.flightbooking.service.payment;

import com.mmt.flightbooking.dto.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Strategy for processing UPI payments
 */
@Component
public class UpiPaymentStrategy implements PaymentStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(UpiPaymentStrategy.class);
    private static final String GATEWAY_NAME = "Razorpay";
    
    @Override
    public PaymentGatewayResponse processPayment(PaymentRequest request) {
        logger.info("Processing UPI payment for ID: {}", request.getUpiId());
        
        try {
            // Validate UPI ID
            if (!validateUpiId(request.getUpiId())) {
                return new PaymentGatewayResponse(false, "Invalid UPI ID", 
                                                null, GATEWAY_NAME);
            }
            
            // Simulate UPI payment with 95% success rate
            boolean success = Math.random() > 0.05;
            
            if (success) {
                String transactionId = "UPI_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                logger.info("UPI payment successful. Transaction ID: {}", transactionId);
                return new PaymentGatewayResponse(true, "UPI payment successful", 
                                                transactionId, GATEWAY_NAME);
            } else {
                logger.warn("UPI payment failed");
                return new PaymentGatewayResponse(false, "UPI payment failed - User declined", 
                                                null, GATEWAY_NAME);
            }
            
        } catch (Exception e) {
            logger.error("UPI payment processing error", e);
            return new PaymentGatewayResponse(false, "UPI payment error: " + e.getMessage(), 
                                            null, GATEWAY_NAME);
        }
    }
    
    @Override
    public String getStrategyName() {
        return "UPI_PAYMENT";
    }
    
    private boolean validateUpiId(String upiId) {
        // Basic UPI ID validation (format: user@bank)
        return upiId != null && upiId.matches("^[\\w.-]+@[\\w.-]+$");
    }
}

