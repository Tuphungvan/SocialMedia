package com.aht.social.infrastructure.messaging.consumer;


import com.aht.social.infrastructure.external.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "email-topic", groupId = "social-group")
    public void consumeEmailEvent(Map<String, String> message) {
        log.info("Đang xử lý gửi email cho: {}", message.get("to"));

        try {
            emailService.sendHtmlEmail(
                    message.get("to"),
                    message.get("subject"),
                    message.get("body")
            );
            log.info("Gửi email thành công!");
        } catch (Exception e) {
            log.error("Gửi email thất bại, Kafka sẽ thử lại hoặc đưa vào DLQ: {}", e.getMessage());
        }
    }
}