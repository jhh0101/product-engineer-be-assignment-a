package com.github.jhh0101.assignment.course.service;

import com.github.jhh0101.assignment.domain.course.client.user.UserCourseClient;
import com.github.jhh0101.assignment.domain.course.dto.CourseResponse;
import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.course.repository.CourseListCondition;
import com.github.jhh0101.assignment.domain.course.repository.CourseRepository;
import com.github.jhh0101.assignment.domain.course.service.CourseService;
import com.github.jhh0101.assignment.domain.user.dto.UserInfoResponse;
import com.github.jhh0101.assignment.domain.user.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
public class CourseListServiceUnitTest {
    @Mock
    CourseRepository courseRepository;

    @InjectMocks
    CourseService courseService;

    @Mock
    private UserCourseClient userCourseClient;

    @Test
    @DisplayName("조회 시 시간이 지난 OPEN 강의는 CLOSED로 상태가 변경되며 조회되어야 함")
    void courseList_statusUpdate_Test() {
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        Course expiredCourse = Course.builder()
                .title("어제 마감된 강의")
                .status(CourseStatus.OPEN)
                .startTime(pastTime)
                .creatorId(1L)
                .build();

        UserInfoResponse mockUser = UserInfoResponse.builder()
                .id(1L)
                .role(Role.CREATOR)
                .name("Test Name")
                .build();

        given(userCourseClient.getUserCourseResponses(anyList()))
                .willReturn(Map.of(1L, mockUser));

        CourseListCondition condition = new CourseListCondition(CourseStatus.CLOSED.name());
        PageRequest pageable = PageRequest.of(0, 10);

        Page<Course> mockPage = new PageImpl<>(List.of(expiredCourse), pageable, 1);
        given(courseRepository.courseListSearch(any(), any(), any())).willReturn(mockPage);

        Page<CourseResponse> result = courseService.courseList(condition, pageable);

        assertThat(result.getContent()).hasSize(1);

        assertThat(result.getContent().getFirst().getStatus()).isEqualTo(CourseStatus.CLOSED);

        assertThat(expiredCourse.getStatus()).isEqualTo(CourseStatus.CLOSED);
    }

    @Test
    @DisplayName("조회 시 시간이 지나지 않은 OPEN 강의는 상태가 변경되지 말아야 함")
    void courseList_status_Test() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(5);
        Course futureCourse = Course.builder()
                .title("미래의 강의")
                .status(CourseStatus.OPEN)
                .startTime(futureTime)
                .creatorId(1L)
                .build();

        UserInfoResponse mockUser = UserInfoResponse.builder()
                .id(1L)
                .role(Role.CREATOR)
                .name("Test Name")
                .build();

        given(userCourseClient.getUserCourseResponses(anyList()))
                .willReturn(Map.of(1L, mockUser));

        Page<Course> mockPage = new PageImpl<>(List.of(futureCourse));
        given(courseRepository.courseListSearch(any(), any(), any())).willReturn(mockPage);

        Page<CourseResponse> result = courseService.courseList(new CourseListCondition(null), PageRequest.of(0, 10));

        assertThat(result.getContent().getFirst().getStatus()).isEqualTo(CourseStatus.OPEN);
        assertThat(futureCourse.getStatus()).isEqualTo(CourseStatus.OPEN);
    }

    @Test
    @DisplayName("조회 시 마감된 강의와 마감 전 강의가 섞여 있어도 각각 올바르게 처리되어야 함")
    void courseList_MixedStatus_Test() {
        Course expired = Course.builder()
                .status(CourseStatus.OPEN)
                .creatorId(1L)
                .startTime(LocalDateTime.now().minusDays(1))
                .build();
        Course active = Course.builder()
                .status(CourseStatus.OPEN)
                .creatorId(1L)
                .startTime(LocalDateTime.now().plusDays(5))
                .build();

        UserInfoResponse mockUser = UserInfoResponse.builder()
                .id(1L)
                .role(Role.CREATOR)
                .name("Test Name")
                .build();

        given(userCourseClient.getUserCourseResponses(anyList()))
                .willReturn(Map.of(1L, mockUser));

        given(courseRepository.courseListSearch(any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(expired, active)));

        Page<CourseResponse> result = courseService.courseList(new CourseListCondition(null), PageRequest.of(0, 10));

        assertThat(result.getContent().get(0).getStatus()).isEqualTo(CourseStatus.CLOSED);
        assertThat(result.getContent().get(1).getStatus()).isEqualTo(CourseStatus.OPEN);
    }
}