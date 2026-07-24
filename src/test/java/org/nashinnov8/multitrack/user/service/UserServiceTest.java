package org.nashinnov8.multitrack.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nashinnov8.multitrack.common.exception.ResourceNotFoundException;
import org.nashinnov8.multitrack.user.domain.User;
import org.nashinnov8.multitrack.user.dto.request.UpdateUserRequest;
import org.nashinnov8.multitrack.user.dto.response.UserResponse;
import org.nashinnov8.multitrack.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private UserService userService;

    private User mockUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        mockUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .displayName("Test User")
                .totalExp(500)
                .level(3)
                .globalStreak(10)
                .timezone("Asia/Ho_Chi_Minh")
                .build();
        mockUser.setId(userId);
    }

    // --- getUserById ---

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        UserResponse response = userService.getUserById(userId);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.id());
        assertEquals("testuser", response.username());
        assertEquals("test@example.com", response.email());
        assertEquals("Test User", response.displayName());
        assertEquals(500, response.totalExp());
        assertEquals(3, response.level());
        assertEquals(10, response.globalStreak());
        assertEquals("Asia/Ho_Chi_Minh", response.timezone());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_Failure_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(userId));
        verify(userRepository).findById(userId);
    }

    // --- updateUser ---

    @Test
    void updateUser_Success() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest("New Display Name", "America/New_York");
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        UserResponse response = userService.updateUser(userId, request);

        // Assert
        assertNotNull(response);
        // Verify the displayName and timezone were changed on the entity
        assertEquals("New Display Name", mockUser.getDisplayName());
        assertEquals("America/New_York", mockUser.getTimezone());
        verify(userRepository).findById(userId);
        verify(userRepository).save(mockUser);
    }

    @Test
    void updateUser_Failure_UserNotFound() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest("New Display Name", null);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(userId, request));
        verify(userRepository, never()).save(any());
    }
}
