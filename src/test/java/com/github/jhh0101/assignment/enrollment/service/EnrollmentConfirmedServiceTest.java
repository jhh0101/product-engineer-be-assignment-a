package com.github.jhh0101.assignment.enrollment.service;

import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.enrollment.client.course.CourseEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.client.user.UserEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.user.dto.UserEnrollmentResponse;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollmentConfirmedServiceTest {
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
        testEnrollment = new Enrollment(
                1L,
                1L,
                1L,
                EnrollmentStatus.PENDING,
                null
        );
    }

    @Test
    @DisplayName("결제 확정 성공 테스트")
    void enrollmentConfirmed_success() {
        Long userId = testEnrollment.getUserId();
        Long enrollmentId = testEnrollment.getId();

        Enrollment spyEnrollment = spy(testEnrollment);

        given(enrollmentRepository.findById(enrollmentId))
                .willReturn(Optional.of(spyEnrollment));

        given(userClient.getUserResponse(userId))
                .willReturn(UserEnrollmentResponse.builder().name("Test Name").build());

        given(courseClient.getCourseResponse(anyLong()))
                .willReturn(CourseEnrollmentResponse.builder().title("Test Title").build());

        EnrollmentConfirmedResponse response = enrollmentService.enrollmentConfirmed(userId, enrollmentId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Title");
        assertThat(response.getName()).isEqualTo("Test Name");
        assertThat(response.getEnrolledAt()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);

        verify(enrollmentRepository, times(1)).findById(1L);
        verify(userClient, times(1)).getUserResponse(anyLong());
        verify(courseClient, times(1)).getCourseResponse(anyLong());
        verify(spyEnrollment, times(1)).enrollmentConfirmed();
    }

    @Test
    @DisplayName("결제 확정 실패 테스트 - 수강 신청 정보를 찾을 수 없음")
    void enrollmentConfirmed_enrollment_not_found() {
        Long userId = 1L;
        Long enrollmentId = 1L;

        given(enrollmentRepository.findById(enrollmentId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.enrollmentConfirmed(userId, enrollmentId))
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ENROLLMENT_NOT_FOUND);

        verify(userClient, times(0)).getUserResponse(anyLong());
        verify(courseClient, times(0)).getCourseResponse(anyLong());

        verifyNoMoreInteractions(userClient, courseClient);
    }

    @Test
    @DisplayName("결제 확정 실패 테스트 - 사용자 정보가 일치하지 않음")
    void enrollmentConfirmed_user_forbidden_access() {
        Long userId = 2L;
        Long enrollmentId = 1L;

        Enrollment spyEnrollment = spy(testEnrollment);

        given(enrollmentRepository.findById(enrollmentId))
                .willReturn(Optional.of(spyEnrollment));

        assertThatThrownBy(() -> enrollmentService.enrollmentConfirmed(userId, enrollmentId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_FORBIDDEN_ACCESS);

        verify(userClient, times(0)).getUserResponse(anyLong());
        verify(courseClient, times(0)).getCourseResponse(anyLong());
        verify(spyEnrollment, times(0)).enrollmentConfirmed();
    }

    @ParameterizedTest
    @EnumSource(value = EnrollmentStatus.class, names = {"CONFIRMED", "CANCELLED"})
    @DisplayName("결제 확정 실패 테스트 - 해당 강의를 이미 신청하거나 취소된 상태")
    void enrollmentConfirmed_not_pending(EnrollmentStatus status) {
        Long userId = 1L;
        Long enrollmentId = 1L;

        Enrollment testEnrollment = new Enrollment(
                1L,
                1L,
                1L,
                status,
                null
        );

        Enrollment spyEnrollment = spy(testEnrollment);

        given(enrollmentRepository.findById(enrollmentId))
                .willReturn(Optional.of(spyEnrollment));

        assertThatThrownBy(() -> enrollmentService.enrollmentConfirmed(userId, enrollmentId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ENROLLMENT_NOT_PENDING);

        verify(userClient, times(0)).getUserResponse(anyLong());
        verify(courseClient, times(0)).getCourseResponse(anyLong());
        verify(spyEnrollment, times(0)).enrollmentConfirmed();
    }

}
