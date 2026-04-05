package com.ticketbus.validation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AntiReplayService {

    private final RedisTemplate<String, String> redisTemplate;

    public boolean isNonceUsed(String nonce) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("nonce:" + nonce));
    }

    public void markNonceUsed(String nonce, Duration ttl) {
        redisTemplate.opsForValue().set("nonce:" + nonce, "1", ttl.getSeconds(), TimeUnit.SECONDS);
    }

    public boolean acquireValidationLock(Long ticketId) {
        Boolean result = redisTemplate.opsForValue()
            .setIfAbsent("lock:ticket:" + ticketId, "1", 10, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    public void releaseValidationLock(Long ticketId) {
        redisTemplate.delete("lock:ticket:" + ticketId);
    }
}
