package com.mmt.flightbooking.repository;

import com.mmt.flightbooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhone(String phone);
    
    Optional<User> findByEmailOrPhone(String email, String phone);
    
    @Query("SELECT u FROM User u WHERE u.email = :email OR u.phone = :phone")
    Optional<User> findByEmailOrPhoneQuery(@Param("email") String email, @Param("phone") String phone);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
}
