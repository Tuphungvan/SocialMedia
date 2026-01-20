package com.aht.social.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.aht.social.domain.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_phone_number", columnList = "phone_number"),
    @Index(name = "idx_google_id", columnList = "google_id"),
    @Index(name = "idx_facebook_id", columnList = "facebook_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 50)
    private String email;

    private String passwordHash;

    @Column(unique = true, length = 20)
    private String phoneNumber;

    @Column(length = 500)
    private String avatarUrl;

    @Column(length = 500)
    private String coverPhotoUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;  // Tiểu sử

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Builder.Default
    private Boolean isVerified = false;

    @Builder.Default
    private Boolean isActive = true;

    @Column(unique = true, length = 100)
    private String googleId;

    @Column(unique = true, length = 100)
    private String facebookId;

    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
