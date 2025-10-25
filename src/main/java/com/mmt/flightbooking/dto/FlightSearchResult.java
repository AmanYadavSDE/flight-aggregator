package com.mmt.flightbooking.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class FlightSearchResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String flightId;
    private String flightNumber;
    private String airline;
    private String airlineCode;
    private String origin;
    private String destination;
    private LocalDate date;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private Integer durationMinutes;
    private Integer stops;
    private BigDecimal price;
    private String currency;
    private Integer availableSeats;
    private String aircraftType;
    private String status;
    
    // Constructors
    public FlightSearchResult() {}
    
    public FlightSearchResult(String flightId, String flightNumber, String airline, 
                            String origin, String destination, LocalDate date,
                            LocalTime departureTime, LocalTime arrivalTime, 
                            BigDecimal price, Integer durationMinutes, Integer stops) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.date = date;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.stops = stops;
    }
    
    // Getters and Setters
    public String getFlightId() {
        return flightId;
    }
    
    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }
    
    public String getFlightNumber() {
        return flightNumber;
    }
    
    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }
    
    public String getAirline() {
        return airline;
    }
    
    public void setAirline(String airline) {
        this.airline = airline;
    }
    
    public String getAirlineCode() {
        return airlineCode;
    }
    
    public void setAirlineCode(String airlineCode) {
        this.airlineCode = airlineCode;
    }
    
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
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public LocalTime getDepartureTime() {
        return departureTime;
    }
    
    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }
    
    public LocalTime getArrivalTime() {
        return arrivalTime;
    }
    
    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    
    public Integer getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public Integer getStops() {
        return stops;
    }
    
    public void setStops(Integer stops) {
        this.stops = stops;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public Integer getAvailableSeats() {
        return availableSeats;
    }
    
    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }
    
    public String getAircraftType() {
        return aircraftType;
    }
    
    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
