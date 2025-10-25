package com.mmt.flightbooking.service.payment;

import com.mmt.flightbooking.dto.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Strategy for processing Wallet payments (Paytm, PhonePe, etc.)
 */
@Component
public class WalletPaymentStrategy implements PaymentStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(WalletPaymentStrategy.class);
    private static final String GATEWAY_NAME = "Razorpay";
    
    @Override
    public PaymentGatewayResponse processPayment(PaymentRequest request) {
        logger.info("Processing wallet payment for type: {}", request.getWalletType());
        
        try {
            // Validate wallet type
            if (request.getWalletType() == null || request.getWalletType().isEmpty()) {
                return new PaymentGatewayResponse(false, "Wallet type is required", 
                                                null, GATEWAY_NAME);
            }
            
            // Simulate wallet payment with 97% success rate
            boolean success = Math.random() > 0.03;
            
            if (success) {
                String transactionId = "WALLET_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                logger.info("Wallet payment successful. Transaction ID: {}", transactionId);
                return new PaymentGatewayResponse(true, "Wallet payment successful", 
                                                transactionId, GATEWAY_NAME);
            } else {
                logger.warn("Wallet payment failed");
                return new PaymentGatewayResponse(false, "Wallet payment failed - Insufficient balance", 
                                                null, GATEWAY_NAME);
            }
            
        } catch (Exception e) {
            logger.error("Wallet payment processing error", e);
            return new PaymentGatewayResponse(false, "Wallet payment error: " + e.getMessage(), 
                                            null, GATEWAY_NAME);
        }
    }
    
    @Override
    public String getStrategyName() {
        return "WALLET_PAYMENT";
    }
}

