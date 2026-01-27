package com.aht.social.domain.entity;

import com.aht.social.domain.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Người nhận thông báo (Chủ bài viết)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // Người gây ra hành động (Người like/share)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    private NotificationType type; // LIKE, SHARE, COMMENT, FRIEND_REQUEST

    // ID của bài viết liên quan (để khi click vào thông báo sẽ dẫn tới bài đó)
    private UUID targetId;

    private String content; // Nội dung hiển thị: "A đã thích bài viết của bạn"

    private boolean isRead = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}