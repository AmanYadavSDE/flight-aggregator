package com.mmt.flightbooking.service.notification;

import com.mmt.flightbooking.entity.Booking;

/**
 * Observer interface for notification listeners
 * Following Observer Pattern for extensible notification system
 */
public interface NotificationListener {
    
    /**
     * Handle booking confirmation notification
     * @param booking Booking details
     */
    void onBookingConfirmed(Booking booking);
    
    /**
     * Handle payment confirmation notification
     * @param booking Booking details
     */
    void onPaymentConfirmed(Booking booking);
    
    /**
     * Handle booking cancellation notification
     * @param booking Booking details
     */
    void onBookingCancelled(Booking booking);
    
    /**
     * Get listener name for logging
     * @return Listener name
     */
    String getListenerName();
    
    /**
     * Check if listener is enabled
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();
}

