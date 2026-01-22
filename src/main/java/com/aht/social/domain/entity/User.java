package com.aht.social.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.aht.social.domain.enums.Gender;
import com.aht.social.domain.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_phone_number", columnList = "phone_number"),
    @Index(name = "idx_google_id", columnList = "google_id"),
    @Index(name = "idx_facebook_id", columnList = "facebook_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false, length = 50)
    String username;

    @Column(unique = true, nullable = false, length = 50)
    String email;

    String passwordHash;

    @Column(unique = true, length = 20)
    String phoneNumber;

    @Column(length = 500)
    String avatarUrl;

    @Column(length = 500)
    String coverPhotoUrl;

    @Column(columnDefinition = "TEXT")
    String bio;

    LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    Gender gender;

    @Builder.Default
    Boolean isVerified = false;

    @Builder.Default
    Boolean isActive = true;

    @Column(unique = true, length = 100)
    String googleId;

    @Column(unique = true, length = 100)
    String facebookId;

    LocalDateTime lastLoginAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    Role role = Role.USER;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;
}
