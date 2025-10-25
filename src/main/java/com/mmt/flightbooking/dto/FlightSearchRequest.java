package com.mmt.flightbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class FlightSearchRequest {
    
    @NotBlank(message = "Origin airport code is required")
    private String origin;
    
    @NotBlank(message = "Destination airport code is required")
    private String destination;
    
    @NotNull(message = "Departure date is required")
    private LocalDate departureDate;
    
    private LocalDate returnDate;
    
    @Positive(message = "Passenger count must be positive")
    private Integer passengerCount = 1;
    
    private String seatClass = "ECONOMY";
    private String sortBy = "PRICE"; // PRICE, DURATION, DEPARTURE_TIME
    private String sortOrder = "ASC"; // ASC, DESC
    
    // Constructors
    public FlightSearchRequest() {}
    
    public FlightSearchRequest(String origin, String destination, LocalDate departureDate) {
        this.origin = origin;
        this.destination = destination;
        this.departureDate = departureDate;
    }
    
    // Getters and Setters
    public String getOrigin() {
        return origin;
    }
    
    public void setOrigin(String origin) {
        this.origin = origin;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public LocalDate getDepartureDate() {
        return departureDate;
    }
    
    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }
    
    public LocalDate getReturnDate() {
        return returnDate;
    }
    
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
    
    public Integer getPassengerCount() {
        return passengerCount;
    }
    
    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
    }
    
    public String getSeatClass() {
        return seatClass;
    }
    
    public void setSeatClass(String seatClass) {
        this.seatClass = seatClass;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
