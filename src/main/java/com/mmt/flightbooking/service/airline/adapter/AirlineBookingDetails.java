package com.mmt.flightbooking.service.airline.adapter;

/**
 * Booking details from airline system
 */
public class AirlineBookingDetails {
    private String pnr;
    private String status;
    private String flightNumber;
    private String origin;
    private String destination;
    
    // Getters and setters
    public String getPnr() { return pnr; }
    public void setPnr(String pnr) { this.pnr = pnr; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
}

