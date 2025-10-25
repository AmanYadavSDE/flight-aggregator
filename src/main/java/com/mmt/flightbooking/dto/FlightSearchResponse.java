package com.mmt.flightbooking.dto;

import java.io.Serializable;
import java.util.List;

public class FlightSearchResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private List<FlightSearchResult> flights;
    private Integer totalCount;
    private String searchId;
    private String message;
    private boolean success = true;
    
    // Constructors
    public FlightSearchResponse() {}
    
    public FlightSearchResponse(List<FlightSearchResult> flights) {
        this.flights = flights;
        this.totalCount = flights.size();
    }
    
    // Getters and Setters
    public List<FlightSearchResult> getFlights() {
        return flights;
    }
    
    public void setFlights(List<FlightSearchResult> flights) {
        this.flights = flights;
        this.totalCount = flights != null ? flights.size() : 0;
    }
    
    public Integer getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
    
    public String getSearchId() {
        return searchId;
    }
    
    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
}
