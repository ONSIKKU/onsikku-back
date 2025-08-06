package com.onsikku.onsikku_back.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {
  private final RedisTemplate<String, Object> redisTemplate;

  public void set(String key, Object value, Duration ttl) {
    redisTemplate.opsForValue().set(key, value, ttl);
  }

  public <T> T get(String key, Class<T> type) {
    Object value = redisTemplate.opsForValue().get(key);
    return type.cast(value);
  }

  public void delete(String key) {
    redisTemplate.delete(key);
  }
}
