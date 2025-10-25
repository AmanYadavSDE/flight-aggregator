package com.mmt.flightbooking.service.notification;

import com.mmt.flightbooking.entity.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SMS notification listener
 */
@Component
public class SmsNotificationListener implements NotificationListener {
    
    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationListener.class);
    
    @Override
    public void onBookingConfirmed(Booking booking) {
        logger.info("Sending SMS notification for booking confirmation: {}", 
                   booking.getBookingReference());
        
        String message = String.format(
            "Booking confirmed! Ref: %s, PNR: %s, Flight: %s, Amount: %s %s. Check email for details.",
            booking.getBookingReference(),
            booking.getAirlinePnr(),
            booking.getFlightNumber(),
            booking.getTotalAmount(),
            booking.getCurrency()
        );
        
        sendSms(booking.getUser().getPhone(), message);
    }
    
    @Override
    public void onPaymentConfirmed(Booking booking) {
        logger.info("Sending SMS notification for payment confirmation: {}", 
                   booking.getBookingReference());
        
        String message = String.format(
            "Payment successful for booking %s. Amount: %s %s. E-ticket sent to your email.",
            booking.getBookingReference(),
            booking.getTotalAmount(),
            booking.getCurrency()
        );
        
        sendSms(booking.getUser().getPhone(), message);
    }
    
    @Override
    public void onBookingCancelled(Booking booking) {
        logger.info("Sending SMS notification for booking cancellation: {}", 
                   booking.getBookingReference());
        
        String message = String.format(
            "Booking %s cancelled. Refund of %s %s will be processed in 5-7 days.",
            booking.getBookingReference(),
            booking.getTotalAmount(),
            booking.getCurrency()
        );
        
        sendSms(booking.getUser().getPhone(), message);
    }
    
    @Override
    public String getListenerName() {
        return "SMS_NOTIFICATION";
    }
    
    @Override
    public boolean isEnabled() {
        return true; // Can be configured via application properties
    }
    
    private void sendSms(String phoneNumber, String message) {
        // Mock SMS sending
        logger.info("SMS sent to: {} | Message: {}", phoneNumber, message);
        // In production: integrate with SMS service (Twilio, AWS SNS, etc.)
    }
}

