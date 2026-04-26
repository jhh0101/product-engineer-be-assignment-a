package com.github.jhh0101.assignment.domain.course.provider;

import com.github.jhh0101.assignment.domain.course.dto.CourseClosedEvent;
import com.github.jhh0101.assignment.domain.course.dto.EnrollmentCancelledEvent;
import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.repository.CourseRepository;
import com.github.jhh0101.assignment.domain.enrollment.client.course.CourseEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CourseProviderImpl implements CourseEnrollmentClient {
    private final CourseRepository courseRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public CourseEnrollmentResponse getCourseResponse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));

        return CourseEnrollmentResponse.from(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, CourseEnrollmentResponse> getCourseResponses(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Course> courses = courseRepository.findAllByIdIn(courseIds);

        if (courses.isEmpty()) {
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
        }

        return courses.stream()
                .collect(Collectors.toMap(
                        Course::getId,
                        CourseEnrollmentResponse::from
                ));
    }

    @Override
    @Transactional
    public void addStudent(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));

        course.addStudent();
    }

    @Override
    @Transactional
    public void subStudent(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));

        course.subStudent();
    }
}
