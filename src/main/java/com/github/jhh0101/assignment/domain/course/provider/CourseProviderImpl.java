package com.github.jhh0101.assignment.domain.course.provider;

import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.repository.CourseRepository;
import com.github.jhh0101.assignment.domain.enrollment.client.course.CourseEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseProviderImpl implements CourseEnrollmentClient {
    private final CourseRepository courseRepository;

    @Override
    public CourseEnrollmentResponse getCourseResponse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));

        return CourseEnrollmentResponse.from(course);
    }

    @Override
    public void addStudent(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));

        course.addStudent();
    }
}
