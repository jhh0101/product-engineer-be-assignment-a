package com.github.jhh0101.assignment.enrollment.service;

import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.enrollment.client.course.CourseEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.client.user.UserEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.repository.EnrollmentRepository;
import com.github.jhh0101.assignment.domain.enrollment.service.EnrollmentService;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CourseRegistrationServiceUnitTest {
    @InjectMocks
    private EnrollmentService enrollmentService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseEnrollmentClient courseClient;

    @Mock
    private UserEnrollmentClient userClient;

    @Test
    @DisplayName("수강 신청 실패 테스트 - 강의를 찾을 수 없음")
    void courseRegistration_course_not_found() {
        Long courseId = 1L;
        Long userId = 1L;

        given(courseClient.getCourseResponse(anyLong()))
                .willThrow(new CustomException(ErrorCode.COURSE_NOT_FOUND));

        assertThatThrownBy(() -> enrollmentService.courseRegistration(userId, courseId))
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COURSE_NOT_FOUND);
    }

    @Test
    @DisplayName("수강 신청 실패 테스트 - 사용자를 찾을 수 없음")
    void courseRegistration_user_not_found() {
        Long courseId = 1L;
        Long userId = 1L;

        given(courseClient.getCourseResponse(anyLong()))
                .willReturn(CourseEnrollmentResponse.builder().maxCapacity(30).status(CourseStatus.OPEN).build());

        given(userClient.getUserResponse(anyLong()))
                .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> enrollmentService.courseRegistration(userId, courseId))
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

}
