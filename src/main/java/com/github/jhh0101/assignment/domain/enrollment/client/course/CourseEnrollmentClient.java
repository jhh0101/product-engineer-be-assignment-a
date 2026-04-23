package com.github.jhh0101.assignment.domain.enrollment.client.course;

import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;

public interface CourseEnrollmentClient {
    CourseEnrollmentResponse getCourseResponse(Long courseId);

    void addStudent(Long courseId);
}
