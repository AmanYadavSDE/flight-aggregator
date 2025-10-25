package com.mmt.flightbooking.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(unique = true, nullable = false)
    private String bookingReference;
    
    // Airline booking references (for aggregator model)
    @Column(name = "airline_code")
    private String airlineCode;  // '6E', 'AI', 'SG', 'UK'
    
    @Column(name = "airline_pnr")
    private String airlinePnr;  // Airline's PNR/booking reference
    
    @Column(name = "airline_booking_id")
    private String airlineBookingId;  // Airline's internal booking ID
    
    // Flight details cached from airline (for quick reference)
    @Column(name = "flight_number")
    private String flightNumber;
    
    @Column(name = "origin_airport")
    private String originAirport;
    
    @Column(name = "destination_airport")
    private String destinationAirport;
    
    @Column(name = "departure_date")
    private java.time.LocalDate departureDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(nullable = false)
    private String currency = "INR";
    
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Passenger> passengers = new ArrayList<>();
    
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Constructors
    public Booking() {}
    
    public Booking(User user, String bookingReference, BigDecimal totalAmount) {
        this.user = user;
        this.bookingReference = bookingReference;
        this.totalAmount = totalAmount;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getBookingReference() {
        return bookingReference;
    }
    
    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }
    
    public String getAirlineCode() {
        return airlineCode;
    }
    
    public void setAirlineCode(String airlineCode) {
        this.airlineCode = airlineCode;
    }
    
    public String getAirlinePnr() {
        return airlinePnr;
    }
    
    public void setAirlinePnr(String airlinePnr) {
        this.airlinePnr = airlinePnr;
    }
    
    public String getAirlineBookingId() {
        return airlineBookingId;
    }
    
    public void setAirlineBookingId(String airlineBookingId) {
        this.airlineBookingId = airlineBookingId;
    }
    
    public String getFlightNumber() {
        return flightNumber;
    }
    
    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }
    
    public String getOriginAirport() {
        return originAirport;
    }
    
    public void setOriginAirport(String originAirport) {
        this.originAirport = originAirport;
    }
    
    public String getDestinationAirport() {
        return destinationAirport;
    }
    
    public void setDestinationAirport(String destinationAirport) {
        this.destinationAirport = destinationAirport;
    }
    
    public java.time.LocalDate getDepartureDate() {
        return departureDate;
    }
    
    public void setDepartureDate(java.time.LocalDate departureDate) {
        this.departureDate = departureDate;
    }
    
    public BookingStatus getStatus() {
        return status;
    }
    
    public void setStatus(BookingStatus status) {
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
    
    public List<Passenger> getPassengers() {
        return passengers;
    }
    
    public void setPassengers(List<Passenger> passengers) {
        this.passengers = passengers;
    }
    
    public Payment getPayment() {
        return payment;
    }
    
    public void setPayment(Payment payment) {
        this.payment = payment;
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
}
