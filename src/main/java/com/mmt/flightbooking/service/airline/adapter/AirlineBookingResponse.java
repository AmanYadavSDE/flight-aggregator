package com.mmt.flightbooking.service.airline.adapter;

/**
 * Response from airline booking API
 */
public class AirlineBookingResponse {
    private boolean success;
    private String pnr;
    private String bookingId;
    private String airlineCode;
    private String message;
    
    public AirlineBookingResponse() {
    }
    
    public AirlineBookingResponse(boolean success, String pnr, String bookingId, 
                                 String airlineCode, String message) {
        this.success = success;
        this.pnr = pnr;
        this.bookingId = bookingId;
        this.airlineCode = airlineCode;
        this.message = message;
    }
    
    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getPnr() { return pnr; }
    public void setPnr(String pnr) { this.pnr = pnr; }
    
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public String getAirlineCode() { return airlineCode; }
    public void setAirlineCode(String airlineCode) { this.airlineCode = airlineCode; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

