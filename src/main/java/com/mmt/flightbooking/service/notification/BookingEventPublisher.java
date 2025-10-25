package com.mmt.flightbooking.service.notification;

import com.mmt.flightbooking.entity.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Publisher for booking events using Observer Pattern
 * Notifies all registered listeners about booking events
 */
@Component
public class BookingEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingEventPublisher.class);
    
    private final List<NotificationListener> listeners;
    
    @Autowired
    public BookingEventPublisher(List<NotificationListener> listeners) {
        this.listeners = listeners;
        logger.info("Booking event publisher initialized with {} listeners", listeners.size());
        listeners.forEach(listener -> 
            logger.info("Registered listener: {}", listener.getListenerName())
        );
    }
    
    /**
     * Publish booking confirmation event to all listeners
     * @param booking Booking that was confirmed
     */
    @Async
    public void publishBookingConfirmed(Booking booking) {
        logger.info("Publishing booking confirmed event for: {}", booking.getBookingReference());
        
        notifyListeners(listener -> {
            try {
                listener.onBookingConfirmed(booking);
            } catch (Exception e) {
                logger.error("Error in listener {} while processing booking confirmation", 
                           listener.getListenerName(), e);
            }
        });
    }
    
    /**
     * Publish payment confirmation event to all listeners
     * @param booking Booking for which payment was confirmed
     */
    @Async
    public void publishPaymentConfirmed(Booking booking) {
        logger.info("Publishing payment confirmed event for: {}", booking.getBookingReference());
        
        notifyListeners(listener -> {
            try {
                listener.onPaymentConfirmed(booking);
            } catch (Exception e) {
                logger.error("Error in listener {} while processing payment confirmation", 
                           listener.getListenerName(), e);
            }
        });
    }
    
    /**
     * Publish booking cancellation event to all listeners
     * @param booking Booking that was cancelled
     */
    @Async
    public void publishBookingCancelled(Booking booking) {
        logger.info("Publishing booking cancelled event for: {}", booking.getBookingReference());
        
        notifyListeners(listener -> {
            try {
                listener.onBookingCancelled(booking);
            } catch (Exception e) {
                logger.error("Error in listener {} while processing booking cancellation", 
                           listener.getListenerName(), e);
            }
        });
    }
    
    /**
     * Add a new listener dynamically (allows runtime extension)
     * @param listener Notification listener to add
     */
    public void addListener(NotificationListener listener) {
        listeners.add(listener);
        logger.info("Added new listener: {}", listener.getListenerName());
    }
    
    /**
     * Remove a listener
     * @param listener Notification listener to remove
     */
    public void removeListener(NotificationListener listener) {
        listeners.remove(listener);
        logger.info("Removed listener: {}", listener.getListenerName());
    }
    
    /**
     * Get count of active listeners
     * @return Number of listeners
     */
    public int getListenerCount() {
        return listeners.size();
    }
    
    private void notifyListeners(java.util.function.Consumer<NotificationListener> action) {
        listeners.stream()
            .filter(NotificationListener::isEnabled)
            .forEach(listener -> {
                logger.debug("Notifying listener: {}", listener.getListenerName());
                action.accept(listener);
            });
    }
}

