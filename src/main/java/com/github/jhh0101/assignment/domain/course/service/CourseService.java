package com.github.jhh0101.assignment.domain.course.service;

import com.github.jhh0101.assignment.domain.course.dto.CourseCreateRequest;
import com.github.jhh0101.assignment.domain.course.dto.CourseResponse;
import com.github.jhh0101.assignment.domain.course.dto.CourseUpdateRequest;
import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.course.repository.CourseRepository;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {
    private final CourseRepository courseRepository;

    @Transactional
    public CourseResponse courseCreate(CourseCreateRequest request) {
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .maxCapacity(request.getMaxCapacity())
                .currentCapacity(0)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(CourseStatus.DRAFT)
                .build();

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new CustomException(ErrorCode.COURSE_INVALID_PERIOD);
        }

        return CourseResponse.from(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse courseUpdate(Long courseId, CourseUpdateRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));

        course.courseUpdate(request);

        return CourseResponse.from(course);
    }
}
