package com.github.jhh0101.assignment.domain.enrollment.service;

import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.enrollment.aop.CheckCourseCapacity;
import com.github.jhh0101.assignment.domain.enrollment.client.course.CourseEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.client.user.UserEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.user.dto.UserEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentRegistrationResponse;
import com.github.jhh0101.assignment.domain.enrollment.entity.Enrollment;
import com.github.jhh0101.assignment.domain.enrollment.entity.EnrollmentStatus;
import com.github.jhh0101.assignment.domain.enrollment.repository.EnrollmentRepository;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseEnrollmentClient courseClient;
    private final UserEnrollmentClient userClient;

    @Transactional
    @CheckCourseCapacity(key = "#courseId")
    public EnrollmentRegistrationResponse courseRegistration(Long userId, Long courseId) {
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new CustomException(ErrorCode.ALREADY_ENROLLED);
        }

        LocalDateTime now = LocalDateTime.now();
        CourseEnrollmentResponse courseResponse = courseClient.getCourseResponse(courseId);
        UserEnrollmentResponse userResponse = userClient.getUserResponse(userId);

        courseClient.addStudent(courseId);

        Enrollment enrollment = Enrollment.builder()
                .userId(userId)
                .courseId(courseId)
                .enrolledAt(now)
                .status(EnrollmentStatus.PENDING)
                .build();

        return EnrollmentRegistrationResponse.from(enrollmentRepository.save(enrollment), userResponse, courseResponse);
    }
}
