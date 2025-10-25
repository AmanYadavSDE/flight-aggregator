package com.mmt.flightbooking.service.payment;

import com.mmt.flightbooking.dto.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Strategy for processing Credit/Debit Card payments
 */
@Component
public class CardPaymentStrategy implements PaymentStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(CardPaymentStrategy.class);
    private static final String GATEWAY_NAME = "Razorpay";
    
    @Override
    public PaymentGatewayResponse processPayment(PaymentRequest request) {
        logger.info("Processing card payment for card ending: {}", 
                   maskCardNumber(request.getCardNumber()));
        
        try {
            // Validate card details
            if (!validateCardDetails(request)) {
                return new PaymentGatewayResponse(false, "Invalid card details", 
                                                null, GATEWAY_NAME);
            }
            
            // Simulate payment processing with 90% success rate
            boolean success = Math.random() > 0.1;
            
            if (success) {
                String transactionId = "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                logger.info("Card payment successful. Transaction ID: {}", transactionId);
                return new PaymentGatewayResponse(true, "Card payment successful", 
                                                transactionId, GATEWAY_NAME);
            } else {
                logger.warn("Card payment declined");
                return new PaymentGatewayResponse(false, "Card payment declined by issuing bank", 
                                                null, GATEWAY_NAME);
            }
            
        } catch (Exception e) {
            logger.error("Card payment processing error", e);
            return new PaymentGatewayResponse(false, "Card payment error: " + e.getMessage(), 
                                            null, GATEWAY_NAME);
        }
    }
    
    @Override
    public String getStrategyName() {
        return "CARD_PAYMENT";
    }
    
    private boolean validateCardDetails(PaymentRequest request) {
        // Basic validation
        return request.getCardNumber() != null && 
               request.getCardNumber().length() >= 13 &&
               request.getCardNumber().length() <= 19;
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }
}

