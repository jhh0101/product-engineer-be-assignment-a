package com.github.jhh0101.assignment.domain.course.event;

import com.github.jhh0101.assignment.domain.course.dto.CourseClosedEvent;
import com.github.jhh0101.assignment.domain.course.dto.CourseOpenedEvent;
import com.github.jhh0101.assignment.domain.course.dto.EnrollmentCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseCapacityHandler {

    private final StringRedisTemplate redisTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCourseOpened(CourseOpenedEvent event) {
        String key = "course:maxCapacity:" + event.courseId();

        redisTemplate.opsForValue().set(key, String.valueOf(event.maxCapacity()));

        log.info("Redis 세팅 완료! 강의 ID: courseId={}", event.courseId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCourseClosed(CourseClosedEvent event) {
        String key = "course:maxCapacity:" + event.courseId();

        redisTemplate.delete(key);

        log.info("Redis 삭제 완료! 강의 ID: courseId={}", event.courseId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enrollmentCancelled(EnrollmentCancelledEvent event) {
        String key = "course:maxCapacity:" + event.courseId();

        redisTemplate.opsForValue().increment(key);
        String maxCapacity = redisTemplate.opsForValue().get(key);

        log.info("남은 신청 가능 수 : capacity={}", maxCapacity);
    }
}