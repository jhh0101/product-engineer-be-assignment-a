package com.github.jhh0101.assignment.domain.course.service;

import com.github.jhh0101.assignment.domain.course.dto.*;
import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.course.repository.CourseListCondition;
import com.github.jhh0101.assignment.domain.course.repository.CourseRepository;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {
    private final CourseRepository courseRepository;
    private final ApplicationEventPublisher eventPublisher;

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

        if (course.getStatus() == CourseStatus.OPEN) {
            eventPublisher.publishEvent(new CourseOpenedEvent(courseId, course.getMaxCapacity() - course.getCurrentCapacity()));
        } else {
            eventPublisher.publishEvent(new CourseClosedEvent(courseId));
        }

        return CourseResponse.from(course);
    }

    @Transactional
    public Page<CourseResponse> courseList(CourseListCondition condition, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();

        Page<Course> courses = courseRepository.courseListSearch(condition, now, pageable);

        courses.getContent().forEach(course -> {
            if (course.getStatus() == CourseStatus.OPEN && course.getStartTime().minusDays(1).isBefore(now)) {
                course.courseClose();
            }
        });

        return courses.map(CourseResponse::from);
    }

    public CourseDetailResponse courseDetail(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));

        return CourseDetailResponse.from(course);
    }
}
