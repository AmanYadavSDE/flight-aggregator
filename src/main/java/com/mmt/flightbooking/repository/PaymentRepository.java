package com.mmt.flightbooking.repository;

import com.mmt.flightbooking.entity.Payment;
import com.mmt.flightbooking.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId")
    Optional<Payment> findByBookingId(@Param("bookingId") Long bookingId);
    
    @Query("SELECT p FROM Payment p WHERE p.method = :method AND p.status = :status")
    List<Payment> findByMethodAndStatus(@Param("method") com.mmt.flightbooking.entity.PaymentMethod method, 
                                       @Param("status") PaymentStatus status);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    Long countByStatus(@Param("status") PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :startDate AND p.createdAt <= :endDate")
    List<Payment> findPaymentsByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                         @Param("endDate") java.time.LocalDateTime endDate);
}
