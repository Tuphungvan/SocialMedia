package com.aht.social.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis operations service cho các thao tác mà Spring Cache không xử lý được:
 * - Distributed lock, Rate limiting, Atomic counter, etc.
 */
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== Basic Operations ====================

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    // ==================== Advanced Operations ====================

    /**
     * Set if absent - dùng cho Distributed Lock
     */
    public boolean setIfAbsent(String key, Object value, Duration ttl) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, ttl));
    }

    /**
     * Atomic increment - dùng cho Counter, Rate Limiting
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }
}
