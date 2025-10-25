package com.mmt.flightbooking.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BookingDetailsResponse {
    
    private Long bookingId;
    private String bookingReference;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FlightInfo> flights;
    private List<PassengerInfo> passengers;
    
    // Constructors
    public BookingDetailsResponse() {}
    
    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
    
    public String getBookingReference() {
        return bookingReference;
    }
    
    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<FlightInfo> getFlights() {
        return flights;
    }
    
    public void setFlights(List<FlightInfo> flights) {
        this.flights = flights;
    }
    
    public List<PassengerInfo> getPassengers() {
        return passengers;
    }
    
    public void setPassengers(List<PassengerInfo> passengers) {
        this.passengers = passengers;
    }
    
    // Inner classes
    public static class FlightInfo {
        private String flightNumber;
        private String airline;
        private String origin;
        private String destination;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private String seatClass;
        private BigDecimal price;
        
        // Getters and Setters
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
        
        public LocalDateTime getDepartureTime() {
            return departureTime;
        }
        
        public void setDepartureTime(LocalDateTime departureTime) {
            this.departureTime = departureTime;
        }
        
        public LocalDateTime getArrivalTime() {
            return arrivalTime;
        }
        
        public void setArrivalTime(LocalDateTime arrivalTime) {
            this.arrivalTime = arrivalTime;
        }
        
        public String getSeatClass() {
            return seatClass;
        }
        
        public void setSeatClass(String seatClass) {
            this.seatClass = seatClass;
        }
        
        public BigDecimal getPrice() {
            return price;
        }
        
        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
    
    public static class PassengerInfo {
        private String firstName;
        private String lastName;
        private Integer age;
        private String type;
        private String seatNumber;
        
        // Getters and Setters
        public String getFirstName() {
            return firstName;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public Integer getAge() {
            return age;
        }
        
        public void setAge(Integer age) {
            this.age = age;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getSeatNumber() {
            return seatNumber;
        }
        
        public void setSeatNumber(String seatNumber) {
            this.seatNumber = seatNumber;
        }
    }
}