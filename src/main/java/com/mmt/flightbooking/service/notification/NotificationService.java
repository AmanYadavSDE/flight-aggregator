package com.mmt.flightbooking.service.notification;

import com.mmt.flightbooking.entity.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Notification Service using Observer Pattern
 * Delegates notification to BookingEventPublisher which notifies all registered listeners
 */
@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private BookingEventPublisher eventPublisher;
    
    /**
     * Send booking confirmation notifications to all channels
     * @param booking Confirmed booking
     */
    public void sendBookingConfirmation(Booking booking) {
        logger.info("Triggering booking confirmation notifications for: {}", 
                   booking.getBookingReference());
        eventPublisher.publishBookingConfirmed(booking);
    }
    
    /**
     * Send payment confirmation notifications to all channels
     * @param booking Booking with confirmed payment
     */
    public void sendPaymentConfirmation(Booking booking) {
        logger.info("Triggering payment confirmation notifications for: {}", 
                   booking.getBookingReference());
        eventPublisher.publishPaymentConfirmed(booking);
    }
    
    /**
     * Send booking cancellation notifications to all channels
     * @param booking Cancelled booking
     */
    public void sendBookingCancellation(Booking booking) {
        logger.info("Triggering booking cancellation notifications for: {}", 
                   booking.getBookingReference());
        eventPublisher.publishBookingCancelled(booking);
    }
}
