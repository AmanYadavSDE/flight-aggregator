package com.mmt.flightbooking.service.payment;

import com.mmt.flightbooking.entity.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PaymentStrategyFactoryTest {

    @Mock
    private CardPaymentStrategy cardPaymentStrategy;

    @Mock
    private UpiPaymentStrategy upiPaymentStrategy;

    @Mock
    private NetBankingPaymentStrategy netBankingPaymentStrategy;

    @Mock
    private WalletPaymentStrategy walletPaymentStrategy;

    private PaymentStrategyFactory paymentStrategyFactory;

    @BeforeEach
    void setUp() {
        lenient().when(cardPaymentStrategy.getStrategyName()).thenReturn("CARD_PAYMENT");
        lenient().when(upiPaymentStrategy.getStrategyName()).thenReturn("UPI_PAYMENT");
        lenient().when(netBankingPaymentStrategy.getStrategyName()).thenReturn("NET_BANKING");
        lenient().when(walletPaymentStrategy.getStrategyName()).thenReturn("WALLET_PAYMENT");

        paymentStrategyFactory = new PaymentStrategyFactory(
            cardPaymentStrategy,
            upiPaymentStrategy,
            netBankingPaymentStrategy,
            walletPaymentStrategy
        );
    }

    @Test
    void testGetStrategy_CreditCard() {
        // Act
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(PaymentMethod.CREDIT_CARD);

        // Assert
        assertNotNull(strategy);
        assertEquals(cardPaymentStrategy, strategy);
    }

    @Test
    void testGetStrategy_DebitCard() {
        // Act
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(PaymentMethod.DEBIT_CARD);

        // Assert
        assertNotNull(strategy);
        assertEquals(cardPaymentStrategy, strategy); // Both credit and debit use same strategy
    }

    @Test
    void testGetStrategy_UPI() {
        // Act
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(PaymentMethod.UPI);

        // Assert
        assertNotNull(strategy);
        assertEquals(upiPaymentStrategy, strategy);
    }

    @Test
    void testGetStrategy_NetBanking() {
        // Act
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(PaymentMethod.NET_BANKING);

        // Assert
        assertNotNull(strategy);
        assertEquals(netBankingPaymentStrategy, strategy);
    }

    @Test
    void testGetStrategy_Wallet() {
        // Act
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(PaymentMethod.WALLET);

        // Assert
        assertNotNull(strategy);
        assertEquals(walletPaymentStrategy, strategy);
    }

    @Test
    void testGetStrategy_UnsupportedMethod() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            paymentStrategyFactory.getStrategy(null);
        });
    }

    @Test
    void testIsSupported_SupportedMethods() {
        // Assert
        assertTrue(paymentStrategyFactory.isSupported(PaymentMethod.CREDIT_CARD));
        assertTrue(paymentStrategyFactory.isSupported(PaymentMethod.DEBIT_CARD));
        assertTrue(paymentStrategyFactory.isSupported(PaymentMethod.UPI));
        assertTrue(paymentStrategyFactory.isSupported(PaymentMethod.NET_BANKING));
        assertTrue(paymentStrategyFactory.isSupported(PaymentMethod.WALLET));
    }

    @Test
    void testIsSupported_UnsupportedMethod() {
        // Assert
        assertFalse(paymentStrategyFactory.isSupported(null));
    }

    @Test
    void testRegisterStrategy_NewPaymentMethod() {
        // Arrange
        PaymentStrategy customStrategy = mock(PaymentStrategy.class);
        when(customStrategy.getStrategyName()).thenReturn("CUSTOM_PAYMENT");
        PaymentMethod customMethod = PaymentMethod.CREDIT_CARD; // Using existing enum for test

        // Act
        paymentStrategyFactory.registerStrategy(customMethod, customStrategy);

        // Assert
        PaymentStrategy retrievedStrategy = paymentStrategyFactory.getStrategy(customMethod);
        assertEquals(customStrategy, retrievedStrategy);
    }

    @Test
    void testRegisterStrategy_OverrideExisting() {
        // Arrange
        PaymentStrategy newCardStrategy = mock(PaymentStrategy.class);
        when(newCardStrategy.getStrategyName()).thenReturn("NEW_CARD_PAYMENT");

        // Act
        paymentStrategyFactory.registerStrategy(PaymentMethod.CREDIT_CARD, newCardStrategy);

        // Assert
        PaymentStrategy retrievedStrategy = paymentStrategyFactory.getStrategy(PaymentMethod.CREDIT_CARD);
        assertEquals(newCardStrategy, retrievedStrategy);
        assertNotEquals(cardPaymentStrategy, retrievedStrategy);
    }

    @Test
    void testFactoryInitialization() {
        // Assert all strategies are registered during initialization
        assertNotNull(paymentStrategyFactory.getStrategy(PaymentMethod.CREDIT_CARD));
        assertNotNull(paymentStrategyFactory.getStrategy(PaymentMethod.DEBIT_CARD));
        assertNotNull(paymentStrategyFactory.getStrategy(PaymentMethod.UPI));
        assertNotNull(paymentStrategyFactory.getStrategy(PaymentMethod.NET_BANKING));
        assertNotNull(paymentStrategyFactory.getStrategy(PaymentMethod.WALLET));
    }

    @Test
    void testGetStrategy_MultipleCallsSameInstance() {
        // Act
        PaymentStrategy strategy1 = paymentStrategyFactory.getStrategy(PaymentMethod.UPI);
        PaymentStrategy strategy2 = paymentStrategyFactory.getStrategy(PaymentMethod.UPI);

        // Assert
        assertSame(strategy1, strategy2);
    }

    @Test
    void testCreditAndDebitCardShareSameStrategy() {
        // Act
        PaymentStrategy creditCardStrategy = paymentStrategyFactory.getStrategy(PaymentMethod.CREDIT_CARD);
        PaymentStrategy debitCardStrategy = paymentStrategyFactory.getStrategy(PaymentMethod.DEBIT_CARD);

        // Assert
        assertSame(creditCardStrategy, debitCardStrategy);
    }
}

