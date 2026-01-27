package com.aht.social.application.dto.event;

import com.aht.social.domain.enums.NotificationType;

import java.util.UUID;

public record NotificationEvent(
        UUID actorId,
        UUID recipientId,
        NotificationType type,
        UUID targetId,
        String content
) {}
