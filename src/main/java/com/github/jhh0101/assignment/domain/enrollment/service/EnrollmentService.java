package com.github.jhh0101.assignment.domain.enrollment.service;

import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.enrollment.aop.CheckCourseCapacity;
import com.github.jhh0101.assignment.domain.enrollment.client.course.CourseEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.client.user.UserEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.user.dto.UserEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentCancelledResponse;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentConfirmedResponse;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentListResponse;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentRegistrationResponse;
import com.github.jhh0101.assignment.domain.enrollment.entity.Enrollment;
import com.github.jhh0101.assignment.domain.enrollment.entity.EnrollmentStatus;
import com.github.jhh0101.assignment.domain.enrollment.repository.EnrollmentRepository;
import com.github.jhh0101.assignment.domain.user.entity.Role;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElse(null);

        if (enrollment != null && enrollment.getStatus() != EnrollmentStatus.CANCELLED) {
            throw new CustomException(ErrorCode.ALREADY_ENROLLED);
        }

        if (enrollment != null) {
            enrollment.reEnroll();
        } else {
            enrollment = Enrollment.builder()
                    .userId(userId)
                    .courseId(courseId)
                    .status(EnrollmentStatus.PENDING)
                    .build();
            enrollmentRepository.save(enrollment);
        }

        courseClient.addStudent(courseId);

        CourseEnrollmentResponse courseResponse = courseClient.getCourseResponse(courseId);
        UserEnrollmentResponse userResponse = userClient.getUserResponse(userId);

        return EnrollmentRegistrationResponse.from(enrollment, userResponse, courseResponse);
    }

    @Transactional
    public EnrollmentConfirmedResponse enrollmentConfirmed(Long userId, Long enrollmentId) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (!enrollment.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.USER_FORBIDDEN_ACCESS);
        }

        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new CustomException(ErrorCode.ENROLLMENT_NOT_PENDING);
        }

        UserEnrollmentResponse userResponse = userClient.getUserResponse(userId);
        CourseEnrollmentResponse courseResponse = courseClient.getCourseResponse(enrollment.getCourseId());

        enrollment.enrollmentConfirmed();

        return EnrollmentConfirmedResponse.from(enrollment, userResponse, courseResponse);
    }

    @Transactional
    public EnrollmentCancelledResponse enrollmentCancelled(Long userId, Long enrollmentId) {
        LocalDateTime now = LocalDateTime.now();

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (!enrollment.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.USER_FORBIDDEN_ACCESS);
        }

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new CustomException(ErrorCode.ENROLLMENT_IS_CANCELLED);
        }

        if (enrollment.getEnrolledAt() != null && enrollment.getEnrolledAt().plusDays(7).isBefore(now)) {
            throw new CustomException(ErrorCode.REFUND_PERIOD_EXPIRED);
        }

        UserEnrollmentResponse userResponse = userClient.getUserResponse(userId);
        CourseEnrollmentResponse courseResponse = courseClient.getCourseResponse(enrollment.getCourseId());

        enrollment.enrollmentCancelled();
        courseClient.subStudent(enrollment.getCourseId());

        return EnrollmentCancelledResponse.from(enrollment, userResponse, courseResponse);
    }

    public Page<EnrollmentListResponse> myEnrollmentList(Long userId, Pageable pageable) {
        Page<Enrollment> enrollments = enrollmentRepository.findAllByUserId(userId, pageable);

        if (enrollments.isEmpty()) {
            return Page.empty(enrollments.getPageable());
        }

        UserEnrollmentResponse userResponse = userClient.getUserResponse(userId);

        List<Long> courseIds = enrollments.stream()
                .map(Enrollment::getCourseId)
                .distinct()
                .toList();

        Map<Long, CourseEnrollmentResponse> courseMap = courseClient.getCourseResponses(courseIds);

        return enrollments.map(enrollment -> {
            CourseEnrollmentResponse courseResponse = courseMap.get(enrollment.getCourseId());
            return EnrollmentListResponse.from(enrollment, userResponse, courseResponse);
        });
    }

    public Page<EnrollmentListResponse> userEnrollmentList(Long userId, Long courseId, Pageable pageable) {
        UserEnrollmentResponse userResponse = userClient.getUserResponse(userId);

        if (userResponse.getRole() != Role.CREATOR) {
            throw new CustomException(ErrorCode.USER_NOT_CREATOR);
        }
        Page<Enrollment> enrollments = enrollmentRepository.findAllByCourseId(courseId, pageable);

        if (enrollments.isEmpty()) {
            return Page.empty(enrollments.getPageable());
        }


        List<Long> courseIds = enrollments.stream()
                .map(Enrollment::getCourseId)
                .distinct()
                .toList();


        List<Long> userIds = enrollments.stream()
                .map(Enrollment::getUserId)
                .distinct()
                .toList();

        Map<Long, CourseEnrollmentResponse> courseMap = courseClient.getCourseResponses(courseIds);
        Map<Long, UserEnrollmentResponse> userMap = userClient.getUserResponses(userIds);

        return enrollments.map(enrollment -> {
            CourseEnrollmentResponse courseMapResponse = courseMap.get(enrollment.getCourseId());
            UserEnrollmentResponse userMapResponse = userMap.get(enrollment.getUserId());
            return EnrollmentListResponse.from(enrollment, userMapResponse, courseMapResponse);
        });
    }
}
