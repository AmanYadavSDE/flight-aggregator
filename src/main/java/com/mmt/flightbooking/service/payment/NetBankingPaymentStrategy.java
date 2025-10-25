package com.mmt.flightbooking.service.payment;

import com.mmt.flightbooking.dto.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Strategy for processing Net Banking payments
 */
@Component
public class NetBankingPaymentStrategy implements PaymentStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(NetBankingPaymentStrategy.class);
    private static final String GATEWAY_NAME = "Razorpay";
    
    @Override
    public PaymentGatewayResponse processPayment(PaymentRequest request) {
        logger.info("Processing net banking payment for bank: {}", request.getBankCode());
        
        try {
            // Validate bank code
            if (request.getBankCode() == null || request.getBankCode().isEmpty()) {
                return new PaymentGatewayResponse(false, "Bank code is required", 
                                                null, GATEWAY_NAME);
            }
            
            // Simulate net banking payment with 92% success rate
            boolean success = Math.random() > 0.08;
            
            if (success) {
                String transactionId = "NB_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                logger.info("Net banking payment successful. Transaction ID: {}", transactionId);
                return new PaymentGatewayResponse(true, "Net banking payment successful", 
                                                transactionId, GATEWAY_NAME);
            } else {
                logger.warn("Net banking payment failed");
                return new PaymentGatewayResponse(false, "Net banking payment failed", 
                                                null, GATEWAY_NAME);
            }
            
        } catch (Exception e) {
            logger.error("Net banking payment processing error", e);
            return new PaymentGatewayResponse(false, "Net banking error: " + e.getMessage(), 
                                            null, GATEWAY_NAME);
        }
    }
    
    @Override
    public String getStrategyName() {
        return "NET_BANKING";
    }
}

