package org.nashinnov8.multitrack.auth.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.nashinnov8.multitrack.auth.domain.RefreshToken;
import org.nashinnov8.multitrack.auth.dto.AuthRequest;
import org.nashinnov8.multitrack.auth.dto.AuthResponse;
import org.nashinnov8.multitrack.auth.dto.RefreshTokenRequest;
import org.nashinnov8.multitrack.auth.dto.RegisterRequest;
import org.nashinnov8.multitrack.auth.repository.RefreshTokenRepository;
import org.nashinnov8.multitrack.common.exception.InvalidRefreshTokenException;
import org.nashinnov8.multitrack.common.exception.UserNotFoundException;
import org.nashinnov8.multitrack.common.jwt.JwtProperties;
import org.nashinnov8.multitrack.common.service.EmailService;
import org.nashinnov8.multitrack.user.domain.User;
import org.nashinnov8.multitrack.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtEncoder jwtEncoder;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtProperties jwtProperties;
  private final EmailService emailService;

  public AuthResponse register(RegisterRequest request) {
    String normalizedEmail = request.email() != null ? request.email().trim().toLowerCase() : "";

    if (userRepository.findByEmail(normalizedEmail).isPresent()) {
      throw new RuntimeException("Email already exists");
    }
    if (userRepository.findByUsername(request.username()).isPresent()) {
      throw new RuntimeException("Username already exists");
    }

    String verificationToken = UUID.randomUUID().toString();
    Instant verificationExpiry = Instant.now().plus(24, ChronoUnit.HOURS);

    User user =
        User.builder()
            .email(normalizedEmail)
            .password(passwordEncoder.encode(request.password()))
            .username(request.username().trim())
            .displayName(request.displayName() != null ? request.displayName().trim() : request.username().trim())
            .enabled(false) // Must verify email first
            .verificationToken(verificationToken)
            .verificationTokenExpiry(verificationExpiry)
            .build();

    userRepository.save(user);

    // Send verification email
    emailService.sendVerificationEmail(user.getEmail(), verificationToken);

    // Return empty tokens indicating account requires verification
    return new AuthResponse(null, null, user.getUsername());
  }

  public boolean verifyEmail(String token) {
    Optional<User> userOpt = userRepository.findByVerificationToken(token);
    if (userOpt.isEmpty()) {
      throw new IllegalArgumentException("Invalid verification token");
    }

    User user = userOpt.get();
    if (user.getVerificationTokenExpiry() != null && user.getVerificationTokenExpiry().isBefore(Instant.now())) {
      throw new IllegalArgumentException("Verification token has expired");
    }

    user.setEnabled(true);
    user.setVerificationToken(null);
    user.setVerificationTokenExpiry(null);
    userRepository.save(user);

    return true;
  }

  public AuthResponse login(AuthRequest request) {
    String normalizedEmail = request.email() != null ? request.email().trim().toLowerCase() : "";
    Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);

    if (userOptional.isEmpty()
        || !passwordEncoder.matches(request.password(), userOptional.get().getPassword())) {
      throw new UserNotFoundException("Invalid email or password");
    }

    User user = userOptional.get();

    if (!user.isEnabled()) {
      throw new RuntimeException("Account is not activated. Please check your email to verify your account.");
    }

    String token = generateToken(user);
    String refreshToken = generateRefreshToken(user);

    return new AuthResponse(token, refreshToken, user.getUsername());
  }

  public AuthResponse refreshToken(RefreshTokenRequest request) {
    Optional<RefreshToken> refreshTokenOptional =
        refreshTokenRepository.findByToken(request.refreshToken());

    if (refreshTokenOptional.isEmpty() || refreshTokenOptional.get().isRevoked()) {
      throw new InvalidRefreshTokenException("Invalid refresh Token");
    }

    RefreshToken oldToken = refreshTokenOptional.get();

    if (oldToken.getExpiryDate().isBefore(Instant.now())) {
      throw new InvalidRefreshTokenException("Refresh token has expired");
    }

    // Revoke the old token (Refresh Token Rotation)
    oldToken.setRevoked(true);
    refreshTokenRepository.save(oldToken);

    // Generate new tokens
    User user = oldToken.getUser();
    String newToken = generateToken(user);
    String newRefreshToken = generateRefreshToken(user);

    return new AuthResponse(newToken, newRefreshToken, user.getUsername());
  }

  private String generateToken(User user) {
    Instant now = Instant.now();
    long expirationSeconds =
        jwtProperties.accessTokenExpirationSeconds() != null
            ? jwtProperties.accessTokenExpirationSeconds()
            : 86400L;

    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("multitrack-api")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expirationSeconds))
            .subject(user.getEmail())
            .claim("username", user.getUsername())
            .claim("userId", user.getId().toString())
            .build();

    return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  private String generateRefreshToken(User user) {
    long expirationSeconds =
        jwtProperties.refreshTokenExpirationSeconds() != null
            ? jwtProperties.refreshTokenExpirationSeconds()
            : 2592000L;

    RefreshToken refreshToken =
        RefreshToken.builder()
            .user(user)
            .token(UUID.randomUUID().toString())
            .expiryDate(Instant.now().plusSeconds(expirationSeconds))
            .revoked(false)
            .build();

    refreshTokenRepository.save(refreshToken);
    return refreshToken.getToken();
  }
}
