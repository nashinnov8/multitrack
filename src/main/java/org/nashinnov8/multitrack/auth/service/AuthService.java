package org.nashinnov8.multitrack.auth.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.nashinnov8.multitrack.auth.dto.AuthRequest;
import org.nashinnov8.multitrack.auth.dto.AuthResponse;
import org.nashinnov8.multitrack.auth.dto.RegisterRequest;
import org.nashinnov8.multitrack.common.exception.UserNotFoundException;
import org.nashinnov8.multitrack.common.jwt.JwtProperties;
import org.nashinnov8.multitrack.user.domain.User;
import org.nashinnov8.multitrack.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    private final JwtProperties jwtProperties;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .username(request.username())
                .displayName(request.displayName())
                .build();

        userRepository.save(user);

        String token = generateToken(user);
        return new AuthResponse(token, user.getUsername());
    }

    public AuthResponse login(AuthRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.email());
        
        if (userOptional.isEmpty() || !passwordEncoder.matches(request.password(), userOptional.get().getPassword())) {
            throw new UserNotFoundException("Invalid email or password");
        }

        User user = userOptional.get();
        String token = generateToken(user);
        
        return new AuthResponse(token, user.getUsername());
    }

    private String generateToken(User user) {
        Instant now = Instant.now();
        // Lấy thời gian sống từ cấu hình, mặc định 86400s (24h) nếu chưa có cấu hình
        long expirationSeconds = jwtProperties.accessTokenExpirationSeconds() != null 
                ? jwtProperties.accessTokenExpirationSeconds() 
                : 86400L;
                
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("multitrack-api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(user.getEmail())
                .claim("username", user.getUsername())
                .claim("userId", user.getId().toString())
                .build();
                
        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
