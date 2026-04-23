package com.github.jhh0101.assignment.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

@TestConfiguration
public class TestRedisConfig {
    private RedisServer redisServer;

    public TestRedisConfig(@Value("${spring.data.redis.port:16379}") int redisPort) {
        this.redisServer = RedisServer.builder()
                .port(redisPort)
                .setting("maxmemory 128M") // 윈도우 실행을 위한 필수 설정!
                .build();
    }

    @PostConstruct
    public void postConstruct() {
        try {
            redisServer.start();
            System.out.println("✅ Embedded Redis가 " + redisServer.ports() + " 포트에서 성공적으로 시작되었습니다.");
        } catch (Exception e) {
            System.err.println("❌ Embedded Redis 시작 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}