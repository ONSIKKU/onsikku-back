package com.onsikku.onsikku_back.global.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisHealthChecker implements CommandLineRunner {

    private final RedisConnectionFactory connectionFactory;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Redis Health Check...");

        try (RedisConnection connection = connectionFactory.getConnection()) {
            String pingResult = connection.ping();
            if ("PONG".equals(pingResult)) {
                log.info("Redis server connected successfully (Status: PONG).");
            } else {
                throw new IllegalStateException("Redis PING failed: " + pingResult);
            }
        } catch (Exception e) {
            log.error("FATAL: Cannot connect to Redis. Shutting down application. Details: {}", e.getMessage());
            // Redis 연결 실패 시 애플리케이션을 강제 종료 (Exit Code 1)
            System.exit(1); 
        }
    }
}