package com.aht.social.application.service;

import com.aht.social.application.dto.request.auth.LoginRequestDTO;
import com.aht.social.application.dto.request.auth.RegisterRequestDTO;
import com.aht.social.application.dto.response.auth.AuthResponseDTO;
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
    public AuthResponseDTO register(RegisterRequestDTO request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã tồn tại");
        }

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

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email không tồn tại"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password for email: {}", request.getEmail());
            throw new UnauthorizedException("Mật khẩu không đúng");
        }

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Tài khoản đã bị vô hiệu hóa");
        }

        log.info("User logged in successfully: {}", user.getId());

        return buildAuthResponse(user);
    }

    private AuthResponseDTO buildAuthResponse(User user) {
        return AuthResponseDTO.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(user))
                .refreshToken(jwtTokenProvider.generateRefreshToken(user))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO refreshToken(String refreshToken) {
        log.info("Refreshing access token");

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new UnauthorizedException("User không tồn tại"));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Tài khoản đã bị vô hiệu hóa");
        }

        // Tạo access token mới
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        
        // Rotate refresh token (giữ nguyên absolute expiry)
        String newRefreshToken = jwtTokenProvider.rotateRefreshToken(refreshToken, user);
        
        log.info("Token refreshed successfully for user: {}", userId);
        
        return AuthResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken) // Token mới nhưng cùng expiry
                .build();
    }
}
