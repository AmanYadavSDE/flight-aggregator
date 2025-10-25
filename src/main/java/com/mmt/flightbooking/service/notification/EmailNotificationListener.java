package com.mmt.flightbooking.service.notification;

import com.mmt.flightbooking.entity.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Email notification listener
 */
@Component
public class EmailNotificationListener implements NotificationListener {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationListener.class);
    
    @Override
    public void onBookingConfirmed(Booking booking) {
        logger.info("Sending email notification for booking confirmation: {}", 
                   booking.getBookingReference());
        
        String emailContent = buildBookingConfirmationEmail(booking);
        sendEmail(booking.getUser().getEmail(), "Booking Confirmation", emailContent);
    }
    
    @Override
    public void onPaymentConfirmed(Booking booking) {
        logger.info("Sending email notification for payment confirmation: {}", 
                   booking.getBookingReference());
        
        String emailContent = buildPaymentConfirmationEmail(booking);
        sendEmail(booking.getUser().getEmail(), "Payment Successful", emailContent);
    }
    
    @Override
    public void onBookingCancelled(Booking booking) {
        logger.info("Sending email notification for booking cancellation: {}", 
                   booking.getBookingReference());
        
        String emailContent = buildCancellationEmail(booking);
        sendEmail(booking.getUser().getEmail(), "Booking Cancelled", emailContent);
    }
    
    @Override
    public String getListenerName() {
        return "EMAIL_NOTIFICATION";
    }
    
    @Override
    public boolean isEnabled() {
        return true; // Can be configured via application properties
    }
    
    private String buildBookingConfirmationEmail(Booking booking) {
        return String.format(
            "Dear %s,\n\n" +
            "Your booking has been confirmed!\n\n" +
            "Booking Reference: %s\n" +
            "Airline PNR: %s\n" +
            "Flight: %s\n" +
            "Route: %s to %s\n" +
            "Date: %s\n" +
            "Total Amount: %s %s\n" +
            "Status: %s\n\n" +
            "Thank you for choosing our service!\n\n" +
            "Best regards,\n" +
            "Flight Booking Team",
            booking.getUser().getEmail(),
            booking.getBookingReference(),
            booking.getAirlinePnr(),
            booking.getFlightNumber(),
            booking.getOriginAirport(),
            booking.getDestinationAirport(),
            booking.getDepartureDate(),
            booking.getTotalAmount(),
            booking.getCurrency(),
            booking.getStatus()
        );
    }
    
    private String buildPaymentConfirmationEmail(Booking booking) {
        return String.format(
            "Dear %s,\n\n" +
            "Your payment has been processed successfully!\n\n" +
            "Booking Reference: %s\n" +
            "Amount Paid: %s %s\n" +
            "Payment Status: Successful\n\n" +
            "Your e-ticket will be sent to you shortly.\n\n" +
            "Best regards,\n" +
            "Flight Booking Team",
            booking.getUser().getEmail(),
            booking.getBookingReference(),
            booking.getTotalAmount(),
            booking.getCurrency()
        );
    }
    
    private String buildCancellationEmail(Booking booking) {
        return String.format(
            "Dear %s,\n\n" +
            "Your booking has been cancelled.\n\n" +
            "Booking Reference: %s\n" +
            "Amount: %s %s\n\n" +
            "Refund will be processed within 5-7 business days.\n\n" +
            "Best regards,\n" +
            "Flight Booking Team",
            booking.getUser().getEmail(),
            booking.getBookingReference(),
            booking.getTotalAmount(),
            booking.getCurrency()
        );
    }
    
    private void sendEmail(String to, String subject, String content) {
        // Mock email sending
        logger.info("Email sent to: {} | Subject: {} | Content: {}", to, subject, content);
        // In production: integrate with email service (SendGrid, AWS SES, etc.)
    }
}

