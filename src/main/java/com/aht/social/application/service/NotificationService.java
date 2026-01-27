package com.aht.social.application.service;

import com.aht.social.application.dto.event.NotificationResponse;
import com.aht.social.application.dto.response.common.PaginationResponse;
import com.aht.social.application.mapper.UserMapper;
import com.aht.social.domain.entity.Notification;
import com.aht.social.domain.entity.User;
import com.aht.social.domain.repository.NotificationRepository;
import com.aht.social.domain.repository.UserRepository;
import com.aht.social.presentation.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Chưa đăng nhập");
        }

        try {
            UUID userId = UUID.fromString(auth.getName());
            return userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("User không tồn tại"));
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Token không hợp lệ");
        }
    }
    @Transactional(readOnly = true)
    public PaginationResponse<NotificationResponse> getMyNotifications(Pageable pageable) {
        User currentUser = getCurrentUser(); // Lấy User đang đăng nhập

        // Lấy thông báo mà người này là người nhận (Recipient)
        Page<Notification> notifyPage = notificationRepository.findByRecipientOrderByCreatedAtDesc(
                currentUser, pageable);

        // Map sang DTO
        Page<NotificationResponse> responsePage = notifyPage.map(notification ->
                NotificationResponse.builder()
                        .id(notification.getId())
                        .actor(userMapper.toPublicResponse(notification.getActor()))
                        .type(notification.getType())
                        .targetId(notification.getTargetId())
                        .content(notification.getContent())
                        .isRead(notification.isRead())
                        .createdAt(notification.getCreatedAt())
                        .build()
        );

        return PaginationResponse.from(responsePage);
    }
}
