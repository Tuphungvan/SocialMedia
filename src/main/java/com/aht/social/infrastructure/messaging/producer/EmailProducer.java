package com.aht.social.infrastructure.messaging.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "email-topic";

    public void sendEmailEvent(String to, String subject, String body) {
        // Tạo một message object chứa thông tin cần gửi
        Map<String, String> emailData = Map.of(
                "to", to,
                "subject", subject,
                "body", body
        );

        kafkaTemplate.send(TOPIC, emailData);
    }
}