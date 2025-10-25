package com.mmt.flightbooking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class CreateBookingRequest {
    
    // For aggregator: flight ID from search results (UUID string from airline)
    @NotEmpty(message = "Flight IDs are required")
    private List<String> flightIds;  // Changed from Long to String for airline UUIDs
    
    @NotNull(message = "Passenger count is required")
    @Positive(message = "Passenger count must be positive")
    private Integer passengerCount;
    
    @Valid
    @NotEmpty(message = "Passenger details are required")
    private List<PassengerRequest> passengers;
    
    @NotNull(message = "Payment method is required")
    private String paymentMethod;
    
    private String contactEmail;
    private String contactPhone;
    private String specialRequests;
    private String mealPreferences;
    
    // Constructors
    public CreateBookingRequest() {}
    
    // Getters and Setters
    public List<String> getFlightIds() {
        return flightIds;
    }
    
    public void setFlightIds(List<String> flightIds) {
        this.flightIds = flightIds;
    }
    
    public Integer getPassengerCount() {
        return passengerCount;
    }
    
    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
    }
    
    public List<PassengerRequest> getPassengers() {
        return passengers;
    }
    
    public void setPassengers(List<PassengerRequest> passengers) {
        this.passengers = passengers;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getSpecialRequests() {
        return specialRequests;
    }
    
    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }
    
    public String getMealPreferences() {
        return mealPreferences;
    }
    
    public void setMealPreferences(String mealPreferences) {
        this.mealPreferences = mealPreferences;
    }
    
    public String getContactEmail() {
        return contactEmail;
    }
    
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    
    public String getContactPhone() {
        return contactPhone;
    }
    
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
}
