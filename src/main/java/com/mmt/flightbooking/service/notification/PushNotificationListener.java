package com.mmt.flightbooking.service.notification;

import com.mmt.flightbooking.entity.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Push notification listener (for mobile apps)
 */
@Component
public class PushNotificationListener implements NotificationListener {
    
    private static final Logger logger = LoggerFactory.getLogger(PushNotificationListener.class);
    
    @Override
    public void onBookingConfirmed(Booking booking) {
        logger.info("Sending push notification for booking confirmation: {}", 
                   booking.getBookingReference());
        
        String title = "Booking Confirmed! ✈️";
        String body = String.format(
            "Your booking %s for flight %s is confirmed. Have a great journey!",
            booking.getBookingReference(),
            booking.getFlightNumber()
        );
        
        sendPushNotification(booking.getUser().getId(), title, body);
    }
    
    @Override
    public void onPaymentConfirmed(Booking booking) {
        logger.info("Sending push notification for payment confirmation: {}", 
                   booking.getBookingReference());
        
        String title = "Payment Successful 💳";
        String body = String.format(
            "Payment of %s %s processed successfully for booking %s",
            booking.getTotalAmount(),
            booking.getCurrency(),
            booking.getBookingReference()
        );
        
        sendPushNotification(booking.getUser().getId(), title, body);
    }
    
    @Override
    public void onBookingCancelled(Booking booking) {
        logger.info("Sending push notification for booking cancellation: {}", 
                   booking.getBookingReference());
        
        String title = "Booking Cancelled";
        String body = String.format(
            "Booking %s has been cancelled. Refund will be processed soon.",
            booking.getBookingReference()
        );
        
        sendPushNotification(booking.getUser().getId(), title, body);
    }
    
    @Override
    public String getListenerName() {
        return "PUSH_NOTIFICATION";
    }
    
    @Override
    public boolean isEnabled() {
        return true; // Can be configured via application properties
    }
    
    private void sendPushNotification(Long userId, String title, String body) {
        // Mock push notification sending
        logger.info("Push notification sent to user: {} | Title: {} | Body: {}", 
                   userId, title, body);
        // In production: integrate with Firebase Cloud Messaging (FCM), Apple Push Notification Service (APNS), etc.
    }
}

