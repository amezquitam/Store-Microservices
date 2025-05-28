package com.example.gateway;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;


@Service
public class RedisCacheService {
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RedisCacheService(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<String> get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Mono<Boolean> set(String key, String value, Duration ttl) {
        return redisTemplate.opsForValue().set(key, value, ttl);
    }
}
