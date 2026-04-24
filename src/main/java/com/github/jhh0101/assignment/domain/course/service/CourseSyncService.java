package com.github.jhh0101.assignment.domain.course.service;

import com.github.jhh0101.assignment.domain.course.dto.CourseCapacityResponse;
import com.github.jhh0101.assignment.domain.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseSyncService {
    private final StringRedisTemplate redisTemplate;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public void syncRedisWithDatabase() {
        List<CourseCapacityResponse> activeCourses = courseRepository.findAllActiveAvailableSeats();

        for (CourseCapacityResponse response : activeCourses) {

            String key = "course:maxCapacity:" + response.getCourseId();
            redisTemplate.opsForValue().set(key, String.valueOf(response.getAvailableSeats()));
        }
    }
}
