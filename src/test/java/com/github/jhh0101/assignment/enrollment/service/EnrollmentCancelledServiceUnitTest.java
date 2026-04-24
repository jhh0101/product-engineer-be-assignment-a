package com.github.jhh0101.assignment.enrollment.service;

import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.enrollment.client.course.CourseEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.client.user.UserEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.user.dto.UserEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentCancelledResponse;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentConfirmedResponse;
import com.github.jhh0101.assignment.domain.enrollment.entity.Enrollment;
import com.github.jhh0101.assignment.domain.enrollment.entity.EnrollmentStatus;
import com.github.jhh0101.assignment.domain.enrollment.repository.EnrollmentRepository;
import com.github.jhh0101.assignment.domain.enrollment.service.EnrollmentService;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollmentCancelledServiceUnitTest {
    @InjectMocks
    private EnrollmentService enrollmentService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseEnrollmentClient courseClient;

    @Mock
    private UserEnrollmentClient userClient;

    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        testEnrollment = new Enrollment(
                1L,
                1L,
                1L,
                EnrollmentStatus.CONFIRMED,
                now,
                0L
        );

    }

    @Test
    @DisplayName("수강 취소 성공 테스트")
    void enrollmentCancelled_success() {
        Long enrollmentId = 1L;
        Long userId = 1L;
        Long courseId = 1L;

        Enrollment spyEnrollment = spy(testEnrollment);

        given(enrollmentRepository.findById(anyLong()))
                .willReturn(Optional.of(spyEnrollment));

        given(userClient.getUserResponse(userId))
                .willReturn(UserEnrollmentResponse.builder().id(userId).name("Test Name").build());

        given(courseClient.getCourseResponse(anyLong()))
                .willReturn(CourseEnrollmentResponse.builder().id(courseId).title("Test Title").build());

        EnrollmentCancelledResponse response = enrollmentService.enrollmentCancelled(userId, enrollmentId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Title");
        assertThat(response.getName()).isEqualTo("Test Name");
        assertThat(response.getEnrolledAt()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);

        verify(enrollmentRepository, times(1)).findById(1L);
        verify(userClient, times(1)).getUserResponse(anyLong());
        verify(courseClient, times(1)).getCourseResponse(anyLong());
        verify(courseClient, times(1)).subStudent(anyLong());
        verify(spyEnrollment, times(1)).enrollmentCancelled();
    }

    @Test
    @DisplayName("수강 취소 실패 테스트 - 수강 신청 정보를 찾을 수 없음")
    void enrollmentCancelled_enrollment_not_found() {
        Long enrollmentId = 1L;
        Long userId = 1L;

        given(enrollmentRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.enrollmentCancelled(userId, enrollmentId))
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ENROLLMENT_NOT_FOUND);

        verify(courseClient, times(0)).subStudent(enrollmentId);
    }

    @Test
    @DisplayName("수강 취소 실패 테스트 - 사용자를 찾을 수 없음")
    void enrollmentCancelled_user_not_found() {
        Long enrollmentId = 1L;
        Long userId = 1L;

        Enrollment spyEnrollment = spy(testEnrollment);

        given(enrollmentRepository.findById(anyLong()))
                .willReturn(Optional.of(spyEnrollment));

        given(userClient.getUserResponse(userId))
                .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> enrollmentService.enrollmentCancelled(userId, enrollmentId))
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(courseClient, times(0)).subStudent(enrollmentId);
        verify(spyEnrollment, times(0)).enrollmentCancelled();
    }

    @Test
    @DisplayName("수강 취소 실패 테스트 - 해당 강의를 찾을 수 없음")
    void enrollmentCancelled_course_not_found() {
        Long enrollmentId = 1L;
        Long userId = 1L;
        Long courseId = 1L;

        Enrollment spyEnrollment = spy(testEnrollment);

        given(enrollmentRepository.findById(anyLong()))
                .willReturn(Optional.of(spyEnrollment));

        given(courseClient.getCourseResponse(courseId))
                .willThrow(new CustomException(ErrorCode.COURSE_NOT_FOUND));

        assertThatThrownBy(() -> enrollmentService.enrollmentCancelled(userId, enrollmentId))
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COURSE_NOT_FOUND);

        verify(courseClient, times(0)).subStudent(enrollmentId);
        verify(spyEnrollment, times(0)).enrollmentCancelled();
    }

    @Test
    @DisplayName("수강 취소 실패 테스트 - 사용자 정보가 일치하지 않음")
    void enrollmentCancelled_user_forbidden_access() {
        Long enrollmentId = 1L;
        Long userId = 2L;

        Enrollment spyEnrollment = spy(testEnrollment);

        given(enrollmentRepository.findById(anyLong()))
                .willReturn(Optional.of(spyEnrollment));

        assertThatThrownBy(() -> enrollmentService.enrollmentCancelled(userId, enrollmentId))
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_FORBIDDEN_ACCESS);

        verify(courseClient, times(0)).subStudent(enrollmentId);
        verify(spyEnrollment, times(0)).enrollmentCancelled();
    }

    @Test
    @DisplayName("수강 취소 실패 테스트 - 해당 강의 신청은 이미 취소됨")
    void enrollmentCancelled_enrollment_is_cancelled() {
        LocalDateTime now = LocalDateTime.now();
        Long enrollmentId = 1L;
        Long userId = 1L;

        testEnrollment = new Enrollment(
                1L,
                1L,
                1L,
                EnrollmentStatus.CANCELLED,
                now,
                0L
        );

        Enrollment spyEnrollment = spy(testEnrollment);

        given(enrollmentRepository.findById(anyLong()))
                .willReturn(Optional.of(spyEnrollment));

        assertThatThrownBy(() -> enrollmentService.enrollmentCancelled(userId, enrollmentId))
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ENROLLMENT_IS_CANCELLED);

        verify(courseClient, times(0)).subStudent(enrollmentId);
        verify(spyEnrollment, times(0)).enrollmentCancelled();
    }

    @Test
    @DisplayName("수강 취소 실패 테스트 - 환불 유효 기간 만료")
    void enrollmentCancelled_refund_period_expired() {
        LocalDateTime now = LocalDateTime.now();
        Long enrollmentId = 1L;
        Long userId = 1L;

        testEnrollment = new Enrollment(
                1L,
                1L,
                1L,
                EnrollmentStatus.CONFIRMED,
                now.minusDays(8),
                0L
        );

        Enrollment spyEnrollment = spy(testEnrollment);

        given(enrollmentRepository.findById(anyLong()))
                .willReturn(Optional.of(spyEnrollment));

        assertThatThrownBy(() -> enrollmentService.enrollmentCancelled(userId, enrollmentId))
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFUND_PERIOD_EXPIRED);

        verify(courseClient, times(0)).subStudent(enrollmentId);
        verify(spyEnrollment, times(0)).enrollmentCancelled();
    }

}
