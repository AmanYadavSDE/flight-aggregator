package com.mmt.flightbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PassengerRequest {
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotNull(message = "Age is required")
    @Positive(message = "Age must be positive")
    private Integer age;
    
    @NotBlank(message = "Passenger type is required")
    private String type; // ADULT, CHILD, INFANT
    
    private String passportNumber;
    private String nationality;
    private String seatNumber;
    private String mealPreference;
    private String specialRequests;
    
    // Constructors
    public PassengerRequest() {}
    
    public PassengerRequest(String firstName, String lastName, Integer age, String type) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.type = type;
    }
    
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
    
    public String getPassportNumber() {
        return passportNumber;
    }
    
    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }
    
    public String getNationality() {
        return nationality;
    }
    
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }
    
    public String getSeatNumber() {
        return seatNumber;
    }
    
    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
    
    public String getMealPreference() {
        return mealPreference;
    }
    
    public void setMealPreference(String mealPreference) {
        this.mealPreference = mealPreference;
    }
    
    public String getSpecialRequests() {
        return specialRequests;
    }
    
    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }
}
