package com.aht.social.infrastructure.kafka;

import com.aht.social.application.dto.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "notification-topic";

    public void sendNotification(NotificationEvent event) {
        log.info("Đang gửi sự kiện đến Kafka topic {}: {}", TOPIC, event);
        kafkaTemplate.send(TOPIC, event);
    }
}