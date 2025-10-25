package com.mmt.flightbooking.service;

import com.mmt.flightbooking.entity.User;
import com.mmt.flightbooking.entity.UserStatus;
import com.mmt.flightbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for managing users
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get or create test user (for demonstration)
     * In production, this would validate JWT token and get user from database
     */
    public User getOrCreateTestUser() {
        return userRepository.findByEmail("test@example.com")
            .orElseGet(() -> {
                User user = new User();
                user.setEmail("test@example.com");
                user.setPhone("+1234567890");
                user.setPasswordHash("$2a$10$dummy");
                user.setStatus(UserStatus.ACTIVE);
                return userRepository.save(user);
            });
    }
    
    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }
}

