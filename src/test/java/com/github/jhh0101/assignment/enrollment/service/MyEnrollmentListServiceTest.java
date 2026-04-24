package com.github.jhh0101.assignment.enrollment.service;

import com.github.jhh0101.assignment.domain.enrollment.client.course.CourseEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.client.user.UserEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.user.dto.UserEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentListResponse;
import com.github.jhh0101.assignment.domain.enrollment.entity.Enrollment;
import com.github.jhh0101.assignment.domain.enrollment.entity.EnrollmentStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class MyEnrollmentListServiceTest {
    @Mock
    EnrollmentRepository enrollmentRepository;

    @InjectMocks
    EnrollmentService enrollmentService;

    @Mock
    private UserEnrollmentClient userClient;

    @Mock
    private CourseEnrollmentClient courseClient;

    @Test
    @DisplayName("내 수강 목록 조회 성공 테스트")
    void myEnrollmentList_success() {
        Long userId = 1L;

        Enrollment testEnrollment1 = new Enrollment(
                1L,
                userId,
                1L,
                EnrollmentStatus.PENDING,
                null,
                1L
        );
        Enrollment testEnrollment2 = new Enrollment(
                2L,
                userId,
                2L,
                EnrollmentStatus.CONFIRMED,
                null,
                1L
        );
        Enrollment testEnrollment3 = new Enrollment(
                3L,
                userId,
                3L,
                EnrollmentStatus.CANCELLED,
                null,
                1L
        );

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Enrollment> mockPage = new PageImpl<>(List.of(testEnrollment1, testEnrollment2, testEnrollment3), pageable, 3);
        given(enrollmentRepository.findAllByUserId(userId, pageable)).willReturn(mockPage);

        given(userClient.getUserResponse(userId))
                .willReturn(UserEnrollmentResponse.builder().name("Test User").build());

        Map<Long, CourseEnrollmentResponse> mockCourseMap = Map.of(
                1L, CourseEnrollmentResponse.builder().id(1L).title("Course 1").build(),
                2L, CourseEnrollmentResponse.builder().id(2L).title("Course 2").build(),
                3L, CourseEnrollmentResponse.builder().id(3L).title("Course 3").build()
        );
        given(courseClient.getCourseResponses(List.of(1L, 2L, 3L))).willReturn(mockCourseMap);

        Page<EnrollmentListResponse> result = enrollmentService.myEnrollmentList(userId, pageable);

        List<EnrollmentListResponse> content = result.getContent();
        assertThat(content).hasSize(3);

        assertThat(content.get(0).getStatus()).isEqualTo(EnrollmentStatus.PENDING);
        assertThat(content.get(0).getTitle()).isEqualTo("Course 1");

        assertThat(content.get(1).getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
        assertThat(content.get(1).getTitle()).isEqualTo("Course 2");

        assertThat(content.get(2).getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThat(content.get(2).getTitle()).isEqualTo("Course 3");
    }

    @Test
    @DisplayName("내 수강 목록 조회 성공 테스트 - 수강 신청 목록이 없음")
    void myEnrollmentList_empty_success() {
        Long userId = 1L;

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Enrollment> mockPage = new PageImpl<>(List.of(), pageable, 0);
        given(enrollmentRepository.findAllByUserId(userId, pageable)).willReturn(mockPage);

        Page<EnrollmentListResponse> result = enrollmentService.myEnrollmentList(userId, pageable);

        List<EnrollmentListResponse> content = result.getContent();
        assertThat(content).hasSize(0);
        assertThat(content).isEmpty();

        verify(courseClient, times(0)).getCourseResponses(anyList());
        verify(userClient, times(0)).getUserResponse(anyLong());
    }

    @Test
    @DisplayName("내 수강 목록 조회 실패 테스트 - 강의를 찾을 수 없음")
    void myEnrollmentList_course_not_found() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Enrollment> mockPage = new PageImpl<>(List.of(new Enrollment(1L, 1L, 999L, EnrollmentStatus.PENDING, null, 1L)), pageable, 1);
        given(enrollmentRepository.findAllByUserId(1L, pageable)).willReturn(mockPage);

        given(courseClient.getCourseResponses(anyList()))
                .willThrow(new CustomException(ErrorCode.COURSE_NOT_FOUND));

        assertThatThrownBy(() -> enrollmentService.myEnrollmentList(1L, pageable))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.COURSE_NOT_FOUND.getMessage());
    }
}