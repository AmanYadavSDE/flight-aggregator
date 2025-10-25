package com.mmt.flightbooking.service.payment;

import com.mmt.flightbooking.dto.PaymentRequest;

/**
 * Strategy interface for different payment methods
 * Following Strategy Pattern for extensible payment processing
 */
public interface PaymentStrategy {
    
    /**
     * Process payment using specific payment method
     * @param request Payment request details
     * @return Payment gateway response
     */
    PaymentGatewayResponse processPayment(PaymentRequest request);
    
    /**
     * Get the name of this payment strategy
     * @return Strategy name for logging/tracking
     */
    String getStrategyName();
}

