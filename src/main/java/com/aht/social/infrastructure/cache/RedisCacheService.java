package com.aht.social.infrastructure.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final StringRedisTemplate redisTemplate; // Dùng StringRedisTemplate cực kỳ ổn định
    private final ObjectMapper objectMapper;

    // Lưu Object dưới dạng JSON String
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, timeout, unit);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi Serialize dữ liệu Redis", e);
        }
    }

    // Lấy JSON String và chuyển ngược lại thành Object
    public <T> T get(String key, Class<T> clazz) {
        String jsonValue = redisTemplate.opsForValue().get(key);
        if (jsonValue == null) return null;
        try {
            return objectMapper.readValue(jsonValue, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi Deserialize dữ liệu Redis", e);
        }
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}