package com.mmt.flightbooking.service.payment;

public class PaymentGatewayResponse {
    
    private boolean success;
    private String message;
    private String transactionId;
    private String response;
    private String gatewayName;
    private String errorMessage;
    
    // Constructors
    public PaymentGatewayResponse() {}
    
    public PaymentGatewayResponse(boolean success, String message, String transactionId, String gatewayName) {
        this.success = success;
        this.message = message;
        this.transactionId = transactionId;
        this.gatewayName = gatewayName;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getResponse() {
        return response;
    }
    
    public void setResponse(String response) {
        this.response = response;
    }
    
    public String getGatewayName() {
        return gatewayName;
    }
    
    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
