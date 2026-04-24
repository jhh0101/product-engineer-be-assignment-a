package com.github.jhh0101.assignment.course.scheduler;

import com.github.jhh0101.assignment.config.TestRedisConfig;
import com.github.jhh0101.assignment.domain.course.service.CourseSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = "schedules.course-sync.delay=1000")
@Import(TestRedisConfig.class)
public class CourseSyncSchedulerTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private CourseSyncService syncService;

    @Test
    @DisplayName("서버 시작 시 ApplicationRunner에 의해 초기 동기화가 실행되어야 한다")
    void startup_sync_test() {
        verify(syncService, atLeastOnce()).syncRedisWithDatabase();
    }

    @Test
    @DisplayName("스케줄러에 의해 주기적으로 동기화 메서드가 호출되어야 한다")
    void scheduled_sync_test() {
        await()
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(syncService, atLeast(3)).syncRedisWithDatabase());
    }
}