package com.github.jhh0101.assignment.config;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestRedisConfig {
    private static final String REDIS_IMAGE = "redis:7.0-alpine";
    private static final int REDIS_PORT = 6379;
    private static final GenericContainer<?> REDIS_CONTAINER;

    static {
        REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
                .withExposedPorts(REDIS_PORT);
        REDIS_CONTAINER.start();

        System.setProperty("spring.data.redis.host", REDIS_CONTAINER.getHost());
        System.setProperty("spring.data.redis.port", String.valueOf(REDIS_CONTAINER.getMappedPort(REDIS_PORT)));

        System.out.println("Testcontainers Redis가 " + REDIS_CONTAINER.getMappedPort(REDIS_PORT) + " 포트에서 성공적으로 시작되었습니다.");
    }

    @PreDestroy
    public void preDestroy() {
        if (REDIS_CONTAINER != null && REDIS_CONTAINER.isRunning()) {
            REDIS_CONTAINER.stop();
            System.out.println("Testcontainers Redis가 종료되었습니다.");
        }
    }
}