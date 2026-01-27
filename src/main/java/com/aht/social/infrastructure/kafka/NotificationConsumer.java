package com.aht.social.infrastructure.kafka;

import com.aht.social.application.dto.event.NotificationEvent;
import com.aht.social.domain.entity.Notification;
import com.aht.social.domain.entity.User;
import com.aht.social.domain.repository.NotificationRepository;
import com.aht.social.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @KafkaListener(topics = "notification-topic", groupId = "social-group")
    public void consumeNotification(NotificationEvent event) {
        log.info("Đã nhận thông báo từ Kafka: {}", event);

        // Không báo khi tự like bài mình
        if (event.actorId().equals(event.recipientId())) return;

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
            log.info("Đã lưu thông báo vào Database thành công!");
        }
    }
}