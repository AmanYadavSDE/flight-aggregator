package com.mmt.flightbooking.service.payment;

import com.mmt.flightbooking.entity.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating appropriate payment strategy based on payment method
 * Follows Factory Pattern for extensible payment strategy selection
 */
@Component
public class PaymentStrategyFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentStrategyFactory.class);
    
    private final Map<PaymentMethod, PaymentStrategy> strategies = new HashMap<>();
    
    @Autowired
    public PaymentStrategyFactory(
            CardPaymentStrategy cardStrategy,
            UpiPaymentStrategy upiStrategy,
            NetBankingPaymentStrategy netBankingStrategy,
            WalletPaymentStrategy walletStrategy) {
        
        // Register all payment strategies
        strategies.put(PaymentMethod.CREDIT_CARD, cardStrategy);
        strategies.put(PaymentMethod.DEBIT_CARD, cardStrategy);
        strategies.put(PaymentMethod.UPI, upiStrategy);
        strategies.put(PaymentMethod.NET_BANKING, netBankingStrategy);
        strategies.put(PaymentMethod.WALLET, walletStrategy);
        
        logger.info("Payment strategies registered: {}", strategies.keySet());
    }
    
    /**
     * Get the appropriate payment strategy for given payment method
     * @param method Payment method
     * @return Payment strategy instance
     * @throws IllegalArgumentException if payment method is not supported
     */
    public PaymentStrategy getStrategy(PaymentMethod method) {
        PaymentStrategy strategy = strategies.get(method);
        
        if (strategy == null) {
            logger.error("No payment strategy found for method: {}", method);
            throw new IllegalArgumentException("Unsupported payment method: " + method);
        }
        
        logger.debug("Selected payment strategy: {} for method: {}", 
                    strategy.getStrategyName(), method);
        return strategy;
    }
    
    /**
     * Register a new payment strategy (allows runtime extension)
     * @param method Payment method
     * @param strategy Payment strategy implementation
     */
    public void registerStrategy(PaymentMethod method, PaymentStrategy strategy) {
        strategies.put(method, strategy);
        logger.info("Registered new payment strategy: {} for method: {}", 
                   strategy.getStrategyName(), method);
    }
    
    /**
     * Check if a payment method is supported
     * @param method Payment method to check
     * @return true if supported, false otherwise
     */
    public boolean isSupported(PaymentMethod method) {
        return strategies.containsKey(method);
    }
}

