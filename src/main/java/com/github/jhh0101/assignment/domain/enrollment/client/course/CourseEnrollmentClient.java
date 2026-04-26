package com.github.jhh0101.assignment.domain.enrollment.client.course;

import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;

import java.util.List;
import java.util.Map;

public interface CourseEnrollmentClient {
    CourseEnrollmentResponse getCourseResponse(Long courseId);

    Map<Long, CourseEnrollmentResponse> getCourseResponses(List<Long> courseIds);

    void addStudent(Long courseId);

    void subStudent(Long courseId);
}
