package org.nashinnov8.multitrack.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nashinnov8.multitrack.auth.domain.RefreshToken;
import org.nashinnov8.multitrack.auth.dto.AuthRequest;
import org.nashinnov8.multitrack.auth.dto.AuthResponse;
import org.nashinnov8.multitrack.auth.repository.RefreshTokenRepository;
import org.nashinnov8.multitrack.common.exception.UserNotFoundException;
import org.nashinnov8.multitrack.common.jwt.JwtProperties;
import org.nashinnov8.multitrack.user.domain.User;
import org.nashinnov8.multitrack.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtEncoder jwtEncoder;

  @Mock private JwtProperties jwtProperties;

  @InjectMocks private AuthService authService;

  private User mockUser;

  @BeforeEach
  void setUp() {
    mockUser =
        User.builder()
            .email("test@example.com")
            .password("encodedPassword")
            .username("testuser")
            .build();
    mockUser.setId(java.util.UUID.randomUUID());
  }

  @Test
  void login_Success() {
    // Arrange
    AuthRequest request = new AuthRequest("test@example.com", "password123");

    when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));
    when(passwordEncoder.matches(request.password(), mockUser.getPassword())).thenReturn(true);
    when(jwtProperties.accessTokenExpirationSeconds()).thenReturn(3600L);
    when(jwtProperties.refreshTokenExpirationSeconds()).thenReturn(86400L);

    Jwt mockJwt = mock(Jwt.class);
    when(mockJwt.getTokenValue()).thenReturn("mocked-jwt-token");
    when(jwtEncoder.encode(any())).thenReturn(mockJwt);
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken());

    // Act
    AuthResponse response = authService.login(request);

    // Assert
    assertNotNull(response);
    assertEquals("testuser", response.username());
    assertEquals("mocked-jwt-token", response.token());
    assertNotNull(response.refreshToken());

    verify(userRepository).findByEmail(request.email());
    verify(passwordEncoder).matches(request.password(), mockUser.getPassword());
    verify(jwtEncoder).encode(any());
    verify(refreshTokenRepository).save(any());
  }

  @Test
  void login_Failure_UserNotFound() {
    // Arrange
    AuthRequest request = new AuthRequest("wrong@example.com", "password123");
    when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UserNotFoundException.class, () -> authService.login(request));

    verify(userRepository).findByEmail(request.email());
    verify(passwordEncoder, never()).matches(anyString(), anyString());
  }

  @Test
  void login_Failure_WrongPassword() {
    // Arrange
    AuthRequest request = new AuthRequest("test@example.com", "wrongpassword");
    when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));
    when(passwordEncoder.matches(request.password(), mockUser.getPassword())).thenReturn(false);

    // Act & Assert
    assertThrows(UserNotFoundException.class, () -> authService.login(request));

    verify(userRepository).findByEmail(request.email());
    verify(passwordEncoder).matches(request.password(), mockUser.getPassword());
    verify(jwtEncoder, never()).encode(any());
  }
}
