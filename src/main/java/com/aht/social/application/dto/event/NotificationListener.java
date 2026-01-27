package com.aht.social.application.dto.event;

import com.aht.social.domain.entity.Notification;
import com.aht.social.domain.entity.User;
import com.aht.social.domain.repository.NotificationRepository;
import com.aht.social.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Async // Rất quan trọng: Chạy bất đồng bộ
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        if (event.actorId().equals(event.recipientId())) return; // Không báo khi tự like bài mình

        User actor = userRepository.findById(event.actorId()).orElse(null);
        User recipient = userRepository.findById(event.recipientId()).orElse(null);

        if (actor != null && recipient != null) {
            Notification notification = Notification.builder()
                    .actor(actor)
                    .recipient(recipient)
                    .type(event.type())
                    .targetId(event.targetId())
                    .content(event.content())
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            log.info("Thông báo mới đã được tạo cho người dùng: {}", recipient.getUsername());

            // Ở đây bạn có thể thêm logic gửi Real-time qua WebSocket hoặc Firebase (FCM)
        }
    }
}
