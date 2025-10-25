package com.mmt.flightbooking.repository;

import com.mmt.flightbooking.entity.Booking;
import com.mmt.flightbooking.entity.BookingStatus;
import com.mmt.flightbooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByUserAndStatus(User user, BookingStatus status);
    
    Optional<Booking> findByBookingReference(String bookingReference);
    
    @Query("SELECT b FROM Booking b WHERE b.user = :user ORDER BY b.createdAt DESC")
    List<Booking> findUserBookings(@Param("user") User user);
    
    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.status = :status ORDER BY b.createdAt DESC")
    List<Booking> findUserBookingsByStatus(@Param("user") User user, @Param("status") BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.status = :status")
    List<Booking> findByStatus(@Param("status") BookingStatus status);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user = :user AND b.status = :status")
    Long countByUserAndStatus(@Param("user") User user, @Param("status") BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.createdAt >= :startDate AND b.createdAt <= :endDate")
    List<Booking> findBookingsByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                         @Param("endDate") java.time.LocalDateTime endDate);
}
