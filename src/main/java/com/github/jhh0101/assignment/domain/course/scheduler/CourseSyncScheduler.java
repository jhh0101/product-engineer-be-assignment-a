package com.github.jhh0101.assignment.domain.course.scheduler;

import com.github.jhh0101.assignment.domain.course.service.CourseSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseSyncScheduler implements ApplicationRunner {
    private final CourseSyncService syncService;

    @Scheduled(fixedDelayString = "${schedules.course-sync.delay:600000}")
    public void syncRedisWithDatabase() {
        syncService.syncRedisWithDatabase();
        System.out.println("Redis 수강 인원 동기화 완료!(10분 주기)");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        syncService.syncRedisWithDatabase();
        System.out.println("Redis 수강 인원 초기 동기화 완료!");
    }
}
