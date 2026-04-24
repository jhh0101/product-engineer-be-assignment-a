package com.github.jhh0101.assignment.domain.course.event;

import com.github.jhh0101.assignment.domain.course.dto.CourseClosedEvent;
import com.github.jhh0101.assignment.domain.course.dto.CourseOpenedEvent;
import com.github.jhh0101.assignment.domain.course.dto.EnrollmentCancelledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CourseCapacityHandler {

    private final StringRedisTemplate redisTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCourseOpened(CourseOpenedEvent event) {
        String key = "course:maxCapacity:" + event.courseId();

        redisTemplate.opsForValue().set(key, String.valueOf(event.maxCapacity()));

        System.out.println("Redis 세팅 완료! 강의 ID: " + event.courseId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCourseClosed(CourseClosedEvent event) {
        String key = "course:maxCapacity:" + event.courseId();

        redisTemplate.delete(key);

        System.out.println("Redis 삭제 완료! 강의 ID: " + event.courseId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enrollmentCancelled(EnrollmentCancelledEvent event) {
        String key = "course:maxCapacity:" + event.courseId();

        redisTemplate.opsForValue().increment(key);
        String maxCapacity = redisTemplate.opsForValue().get(key);

        System.out.println("Redis 업데이트 완료! 강의 ID: " + event.courseId());
        System.out.println("남은 신청 가능 수 : " + maxCapacity);
    }
}