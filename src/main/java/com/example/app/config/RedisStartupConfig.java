package com.example.app.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStartupConfig implements ApplicationRunner {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public void run(ApplicationArguments args) {
        try {
            redisConnectionFactory.getConnection().serverCommands().flushAll();
            log.info("Redis cache cleared on startup");
        } catch (Exception e) {
            log.warn("Could not flush Redis on startup: {}", e.getMessage());
        }
    }
}