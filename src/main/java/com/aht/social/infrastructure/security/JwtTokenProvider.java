package com.aht.social.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aht.social.domain.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; 

    private SecretKey secretKey;

    @PostConstruct
    public void init(){
        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT Secret Key initialized successfully");
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);
        
        return Jwts.builder()
            .subject(user.getId().toString())
            .issuedAt(now)              
            .expiration(expiryDate)         
            .signWith(secretKey)
            .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);
        
        return Jwts.builder()
            .subject(user.getId().toString())
            .claim("absoluteExpiry", expiryDate.getTime()) // Lưu thời gian hết hạn tuyệt đối
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            // Kiểm tra absolute expiry (nếu có)
            if (claims.containsKey("absoluteExpiry")) {
                Long absoluteExpiry = claims.get("absoluteExpiry", Long.class);
                if (System.currentTimeMillis() > absoluteExpiry) {
                    log.error("Token exceeded absolute expiration time");
                    return false;
                }
            }
            
            return true;
            
        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        
        return false;
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        
        return claims.getSubject();  // Lấy claim "sub"
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        
        return claims.getExpiration();
    }

    public String rotateRefreshToken(String oldRefreshToken, User user) {
        // Lấy absolute expiry từ token cũ
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(oldRefreshToken)
            .getPayload();
        
        Long absoluteExpiry = claims.get("absoluteExpiry", Long.class);
        Date absoluteExpiryDate = new Date(absoluteExpiry);
        
        // Tạo token mới nhưng GIỮ NGUYÊN absolute expiry
        Date now = new Date();
        
        return Jwts.builder()
            .subject(user.getId().toString())
            .claim("absoluteExpiry", absoluteExpiry) // Giữ nguyên thời gian hết hạn gốc
            .issuedAt(now)
            .expiration(absoluteExpiryDate) // Expiry giống token cũ
            .signWith(secretKey)
            .compact();
    }
}
