package com.mmt.flightbooking.service;

import com.mmt.flightbooking.entity.User;
import com.mmt.flightbooking.entity.UserStatus;
import com.mmt.flightbooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPhone("+1234567890");
        testUser.setPasswordHash("$2a$10$dummy");
        testUser.setStatus(UserStatus.ACTIVE);
    }

    @Test
    void testGetOrCreateTestUser_ExistingUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getOrCreateTestUser();

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("+1234567890", result.getPhone());
        assertEquals(UserStatus.ACTIVE, result.getStatus());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testGetOrCreateTestUser_NewUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // Act
        User result = userService.getOrCreateTestUser();

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("+1234567890", result.getPhone());
        assertEquals("$2a$10$dummy", result.getPasswordHash());
        assertEquals(UserStatus.ACTIVE, result.getStatus());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testGetOrCreateTestUser_CreatesWithCorrectFields() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenReturn(testUser);

        // Act
        userService.getOrCreateTestUser();

        // Assert
        User createdUser = userCaptor.getValue();
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals("+1234567890", createdUser.getPhone());
        assertEquals("$2a$10$dummy", createdUser.getPasswordHash());
        assertEquals(UserStatus.ACTIVE, createdUser.getStatus());
    }

    @Test
    void testGetUserByEmail_Success() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals("test@example.com", result.getEmail());

        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testGetUserByEmail_NotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserByEmail("nonexistent@example.com");
        });

        assertTrue(exception.getMessage().contains("User not found"));
        assertTrue(exception.getMessage().contains("nonexistent@example.com"));
    }

    @Test
    void testGetUserByEmail_NullEmail() {
        // Arrange
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.getUserByEmail(null);
        });
    }

    @Test
    void testGetUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserById(999L);
        });

        assertTrue(exception.getMessage().contains("User not found"));
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    void testGetUserById_NullId() {
        // Arrange
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.getUserById(null);
        });
    }

    @Test
    void testGetOrCreateTestUser_MultipleCallsSameUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        User result1 = userService.getOrCreateTestUser();
        User result2 = userService.getOrCreateTestUser();

        // Assert
        assertEquals(result1.getId(), result2.getId());
        verify(userRepository, times(2)).findByEmail("test@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testGetUserByEmail_DifferentEmails() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        when(userRepository.findByEmail("user1@example.com")).thenReturn(Optional.of(user1));
        when(userRepository.findByEmail("user2@example.com")).thenReturn(Optional.of(user2));

        // Act
        User result1 = userService.getUserByEmail("user1@example.com");
        User result2 = userService.getUserByEmail("user2@example.com");

        // Assert
        assertEquals(1L, result1.getId());
        assertEquals(2L, result2.getId());
        assertEquals("user1@example.com", result1.getEmail());
        assertEquals("user2@example.com", result2.getEmail());
    }

    @Test
    void testGetUserById_DifferentIds() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        // Act
        User result1 = userService.getUserById(1L);
        User result2 = userService.getUserById(2L);

        // Assert
        assertEquals(1L, result1.getId());
        assertEquals(2L, result2.getId());
    }
}

