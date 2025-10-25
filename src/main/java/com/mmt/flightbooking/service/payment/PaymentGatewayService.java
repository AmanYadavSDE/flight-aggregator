package com.mmt.flightbooking.service.payment;

import com.mmt.flightbooking.dto.PaymentRequest;
import com.mmt.flightbooking.entity.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Payment Gateway Service using Strategy Pattern
 * This service delegates payment processing to appropriate strategy based on payment method
 */
@Service
public class PaymentGatewayService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayService.class);
    
    @Autowired
    private PaymentStrategyFactory strategyFactory;
    
    /**
     * Process payment using appropriate strategy based on payment method
     * @param request Payment request containing method and details
     * @return Payment gateway response
     */
    public PaymentGatewayResponse processPayment(PaymentRequest request) {
        logger.info("Processing payment through gateway for method: {}", 
                   request.getPayment().getMethod());
        
        try {
            PaymentMethod method = request.getPayment().getMethod();
            
            // Check if payment method is supported
            if (!strategyFactory.isSupported(method)) {
                logger.error("Unsupported payment method: {}", method);
                return new PaymentGatewayResponse(false, "Unsupported payment method: " + method, 
                                                null, "System");
            }
            
            // Get appropriate strategy and process payment
            PaymentStrategy strategy = strategyFactory.getStrategy(method);
            logger.debug("Using strategy: {} for payment method: {}", 
                        strategy.getStrategyName(), method);
            
            PaymentGatewayResponse response = strategy.processPayment(request);
            
            logger.info("Payment processing completed. Success: {}, Transaction: {}", 
                       response.isSuccess(), response.getTransactionId());
            
            return response;
            
        } catch (Exception e) {
            logger.error("Payment gateway processing failed", e);
            return new PaymentGatewayResponse(false, "Payment gateway error: " + e.getMessage(), 
                                            null, "System");
        }
    }
}
