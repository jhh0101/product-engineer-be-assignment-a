package com.github.jhh0101.assignment.domain.course.event;

import com.github.jhh0101.assignment.domain.course.dto.CourseClosedEvent;
import com.github.jhh0101.assignment.domain.course.dto.CourseOpenedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseCapacityHandler {

    private final StringRedisTemplate redisTemplate;

    @EventListener
    public void handleCourseOpened(CourseOpenedEvent event) {
        String key = "course:maxCapacity:" + event.courseId();

        redisTemplate.opsForValue().set(key, String.valueOf(event.maxCapacity()));

        System.out.println("Redis 세팅 완료! 강의 ID: " + event.courseId());
    }

    @EventListener
    public void handleCourseClosed(CourseClosedEvent event) {
        String key = "course:maxCapacity:" + event.courseId();

        redisTemplate.delete(key);

        System.out.println("Redis 삭제 완료! 강의 ID: " + event.courseId());
    }
}