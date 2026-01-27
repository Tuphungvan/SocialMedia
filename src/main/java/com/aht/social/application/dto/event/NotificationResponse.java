package com.aht.social.application.dto.event;

import com.aht.social.application.dto.response.post.UserPublicResponse;
import com.aht.social.domain.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private UserPublicResponse actor; // Thông tin người like/share (Avatar, Name)
    private NotificationType type;
    private UUID targetId;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;
}
