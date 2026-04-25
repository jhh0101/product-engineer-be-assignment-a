package com.github.jhh0101.assignment.enrollment.service;

import com.github.jhh0101.assignment.domain.enrollment.client.course.CourseEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.client.user.UserEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentListResponse;
import com.github.jhh0101.assignment.domain.enrollment.entity.Enrollment;
import com.github.jhh0101.assignment.domain.enrollment.entity.EnrollmentStatus;
import com.github.jhh0101.assignment.domain.enrollment.repository.EnrollmentRepository;
import com.github.jhh0101.assignment.domain.enrollment.service.EnrollmentService;
import com.github.jhh0101.assignment.domain.user.dto.UserInfoResponse;
import com.github.jhh0101.assignment.domain.user.entity.Role;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class UserEnrollmentListServiceTest {
    @Mock
    EnrollmentRepository enrollmentRepository;

    @InjectMocks
    EnrollmentService enrollmentService;

    @Mock
    private UserEnrollmentClient userClient;

    @Mock
    private CourseEnrollmentClient courseClient;

    @Test
    @DisplayName("강의별 수강자 목록 조회 성공 테스트")
    void userEnrollmentList_success() {
        Long userId = 1L;
        Long courseId = 1L;

        Enrollment testEnrollment1 = new Enrollment(
                1L,
                1L,
                1L,
                EnrollmentStatus.PENDING,
                null,
                1L
        );
        Enrollment testEnrollment2 = new Enrollment(
                2L,
                2L,
                1L,
                EnrollmentStatus.CONFIRMED,
                null,
                1L
        );
        Enrollment testEnrollment3 = new Enrollment(
                3L,
                3L,
                1L,
                EnrollmentStatus.CANCELLED,
                null,
                1L
        );

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Enrollment> mockPage = new PageImpl<>(List.of(testEnrollment1, testEnrollment2, testEnrollment3), pageable, 3);
        given(enrollmentRepository.findAllByCourseId(courseId, pageable)).willReturn(mockPage);

        given(userClient.getUserResponse(userId))
                .willReturn(UserInfoResponse.builder().role(Role.CREATOR).name("Test Creator").build());

        given(courseClient.getCourseResponse(courseId))
                .willReturn(CourseEnrollmentResponse.builder().title("Test Title").creatorId(userId).build());

        Map<Long, UserInfoResponse> mockUserMap = Map.of(
                1L, UserInfoResponse.builder().id(1L).name("Test User1").build(),
                2L, UserInfoResponse.builder().id(2L).name("Test User2").build(),
                3L, UserInfoResponse.builder().id(3L).name("Test User3").build()
        );
        given(userClient.getUserResponses(List.of(1L, 2L, 3L))).willReturn(mockUserMap);

        Page<EnrollmentListResponse> result = enrollmentService.userEnrollmentList(userId, courseId, pageable);

        List<EnrollmentListResponse> content = result.getContent();
        assertThat(content).hasSize(3);

        assertThat(content.get(0).getStatus()).isEqualTo(EnrollmentStatus.PENDING);
        assertThat(content.get(0).getName()).isEqualTo("Test User1");

        assertThat(content.get(1).getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
        assertThat(content.get(1).getName()).isEqualTo("Test User2");

        assertThat(content.get(2).getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThat(content.get(2).getName()).isEqualTo("Test User3");

        verify(courseClient, times(1)).getCourseResponse(anyLong());
        verify(userClient, times(1)).getUserResponse(anyLong());
        verify(userClient, times(1)).getUserResponses(anyList());
    }

    @Test
    @DisplayName("강의별 수강자 목록 조회 성공 테스트 - 수강 신청 목록이 없음")
    void userEnrollmentList_empty_success() {
        Long userId = 1L;
        Long courseId = 1L;

        given(userClient.getUserResponse(userId))
                .willReturn(UserInfoResponse.builder().role(Role.CREATOR).name("Test Creator").build());

        given(courseClient.getCourseResponse(courseId))
                .willReturn(CourseEnrollmentResponse.builder().creatorId(userId).title("Test Title").build());

        PageRequest pageable = PageRequest.of(0, 10);
        Page<Enrollment> mockPage = new PageImpl<>(List.of(), pageable, 0);
        given(enrollmentRepository.findAllByCourseId(courseId, pageable)).willReturn(mockPage);

        Page<EnrollmentListResponse> result = enrollmentService.userEnrollmentList(userId, courseId, pageable);

        List<EnrollmentListResponse> content = result.getContent();
        assertThat(content).hasSize(0);
        assertThat(content).isEmpty();

        verify(courseClient, times(1)).getCourseResponse(anyLong());
        verify(userClient, times(1)).getUserResponse(anyLong());
        verify(userClient, times(0)).getUserResponses(anyList());
    }

    @Test
    @DisplayName("강의별 수강자 목록 조회 실패 테스트 - 사용자를 찾을 수 없음")
    void userEnrollmentList_user_not_found() {
        PageRequest pageable = PageRequest.of(0, 10);

        given(userClient.getUserResponse(anyLong()))
                .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> enrollmentService.userEnrollmentList(1L, 1L, pageable))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("강의별 수강자 목록 조회 실패 테스트 - Creator 권한을 가지고 있지 않음")
    void userEnrollmentList_user_not_creator() {
        given(userClient.getUserResponse(1L))
                .willReturn(UserInfoResponse.builder().role(Role.USER).name("Test Creator").build());

        PageRequest pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> enrollmentService.userEnrollmentList(1L, 1L, pageable))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_CREATOR.getMessage());
    }

    @Test
    @DisplayName("강의별 수강자 목록 조회 실패 테스트 - 강의를 찾을 수 없음")
    void userEnrollmentList_course_not_found() {
        given(userClient.getUserResponse(1L))
                .willReturn(UserInfoResponse.builder().role(Role.CREATOR).name("Test Creator").build());

        given(courseClient.getCourseResponse(anyLong()))
                .willThrow(new CustomException(ErrorCode.COURSE_NOT_FOUND));

        PageRequest pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> enrollmentService.userEnrollmentList(1L, 1L, pageable))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.COURSE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("강의별 수강자 목록 조회 실패 테스트 - 담당 강사 정보가 일치하지 않음")
    void userEnrollmentList_course_not_owner() {
        given(userClient.getUserResponse(1L))
                .willReturn(UserInfoResponse.builder().role(Role.CREATOR).name("Test Creator").build());

        given(courseClient.getCourseResponse(1L))
                .willReturn(CourseEnrollmentResponse.builder().creatorId(2L).title("Test Title").build());

        PageRequest pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> enrollmentService.userEnrollmentList(1L, 1L, pageable))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.NOT_COURSE_OWNER.getMessage());
    }

    @Test
    @DisplayName("강의별 수강자 목록 조회 실패 테스트 - 수강생 유저 정보를 찾을 수 없음")
    void userEnrollmentList_courseList_not_found() {
        given(userClient.getUserResponse(1L))
                .willReturn(UserInfoResponse.builder().role(Role.CREATOR).name("Test Creator").build());

        given(courseClient.getCourseResponse(1L))
                .willReturn(CourseEnrollmentResponse.builder().creatorId(1L).title("Test Title").build());

        PageRequest pageable = PageRequest.of(0, 10);
        Page<Enrollment> mockPage = new PageImpl<>(List.of(Enrollment.builder().build()), pageable, 1);
        given(enrollmentRepository.findAllByCourseId(1L, pageable)).willReturn(mockPage);

        given(userClient.getUserResponses(anyList()))
                .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> enrollmentService.userEnrollmentList(1L, 1L, pageable))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }
}