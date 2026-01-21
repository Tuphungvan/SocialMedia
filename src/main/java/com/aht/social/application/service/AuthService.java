package com.aht.social.application.service;

import com.aht.social.application.dto.request.auth.LoginRequest;
import com.aht.social.application.dto.request.auth.RegisterRequest;
import com.aht.social.application.dto.response.auth.AuthResponse;
import com.aht.social.application.dto.response.auth.UserResponse;
import com.aht.social.domain.entity.User;
import com.aht.social.domain.enums.Role;
import com.aht.social.domain.repository.UserRepository;
import com.aht.social.infrastructure.security.JwtTokenProvider;
import com.aht.social.presentation.exception.BadRequestException;
import com.aht.social.presentation.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        
        // Check email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        
        // Create user
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(Role.USER)
            .isActive(true)
            .isVerified(false)
            .build();
        
        user = userRepository.save(user);
        
        log.info("User registered successfully: {}", user.getId());
        
        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(900000L)
            .user(mapToUserResponse(user))
            .build();
    }
    
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        
        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password for email: {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }
        
        // Check active
        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is disabled");
        }
        
        log.info("User logged in successfully: {}", user.getId());
        
        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(900000L)
            .user(mapToUserResponse(user))
            .build();
    }
    
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .avatarUrl(user.getAvatarUrl())
            .role(user.getRole())
            .isVerified(user.getIsVerified())
            .build();
    }
}
