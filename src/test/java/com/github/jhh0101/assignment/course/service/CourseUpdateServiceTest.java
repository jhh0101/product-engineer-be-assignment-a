package com.github.jhh0101.assignment.course.service;

import com.github.jhh0101.assignment.domain.course.dto.CourseCreateRequest;
import com.github.jhh0101.assignment.domain.course.dto.CourseResponse;
import com.github.jhh0101.assignment.domain.course.dto.CourseUpdateRequest;
import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.course.repository.CourseRepository;
import com.github.jhh0101.assignment.domain.course.service.CourseService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class CourseUpdateServiceTest {
    @Mock
    CourseRepository courseRepository;

    @InjectMocks
    CourseService courseService;

    private Course testCourse;

    @BeforeEach
    void setUp() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        testCourse = new Course(
                1L,
                "Test Title",
                "Test Description",
                150000,
                50,
                0,
                now,
                now.plusMonths(5),
                CourseStatus.DRAFT
        );
    }

    @Test
    @DisplayName("강의 수정 성공 테스트")
    void courseUpdate_success() {
        given(courseRepository.findById(1L))
                .willReturn(Optional.of(testCourse));

        LocalDateTime now = LocalDateTime.now();

        CourseUpdateRequest request = new CourseUpdateRequest(
                null,
                "Test Description",
                250000,
                50,
                now,
                now.plusMonths(5),
                CourseStatus.OPEN
        );

        CourseResponse response = courseService.courseUpdate(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo(testCourse.getTitle());
        assertThat(response.getPrice()).isEqualTo(250000);
        assertThat(response.getStatus()).isEqualTo(CourseStatus.OPEN);
        assertThat(response.getDescription()).isEqualTo("Test Description");
        assertThat(response.getMaxCapacity()).isEqualTo(50);

        verify(courseRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("강의 수정 실패 테스트 - 종료일이 시작일보다 빠름")
    void courseUpdate_startTime_Period() {
        given(courseRepository.findById(1L))
                .willReturn(Optional.of(testCourse));

        LocalDateTime now = LocalDateTime.now();

        CourseUpdateRequest request = new CourseUpdateRequest(
                null,
                "Test Description",
                250000,
                50,
                now,
                now.minusDays(5),
                CourseStatus.OPEN
        );

        assertThatThrownBy(() -> courseService.courseUpdate(1L, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COURSE_INVALID_PERIOD);
    }

    @Test
    @DisplayName("강의 수정 실패 테스트 - 강의가 존재하지 않음")
    void courseUpdate_not_found() {
        given(courseRepository.findById(1L))
                .willReturn(Optional.empty());

        LocalDateTime now = LocalDateTime.now();

        CourseUpdateRequest request = new CourseUpdateRequest(
                null,
                "Test Description",
                250000,
                50,
                now,
                now.plusMonths(5),
                CourseStatus.OPEN
        );

        assertThatThrownBy(() -> courseService.courseUpdate(1L, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COURSE_NOT_FOUND);
    }

    @Test
    @DisplayName("강의 수정 실패 테스트 - 강의 상태 변경 불가")
    void courseUpdate_status_change_error() {
        LocalDateTime now = LocalDateTime.now();

        Course testCourse2 = new Course(
                1L,
                "Test Title",
                "Test Description",
                150000,
                50,
                0,
                now,
                now.plusMonths(5),
                CourseStatus.CLOSED
        );

        given(courseRepository.findById(1L))
                .willReturn(Optional.of(testCourse2));

        CourseUpdateRequest request = new CourseUpdateRequest(
                null,
                "Test Description",
                250000,
                50,
                now,
                now.plusMonths(5),
                CourseStatus.OPEN
        );

        assertThatThrownBy(() -> courseService.courseUpdate(1L, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COURSE_STATUS_CHANGE_ERROR);
    }

    @Test
    @DisplayName("강의 수정 실패 테스트 - 수강자 최대 인원 변경 불가")
    void courseUpdate_invalid_maxCapacity() {
        LocalDateTime now = LocalDateTime.now();

        Course testCourse2 = new Course(
                1L,
                "Test Title",
                "Test Description",
                150000,
                100,
                50,
                now,
                now.plusMonths(5),
                CourseStatus.OPEN
        );

        given(courseRepository.findById(1L))
                .willReturn(Optional.of(testCourse2));

        CourseUpdateRequest request = new CourseUpdateRequest(
                null,
                "Test Description",
                250000,
                20,
                now,
                now.plusMonths(5),
                null
        );

        assertThatThrownBy(() -> courseService.courseUpdate(1L, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CAPACITY_UPDATE);
    }

    @Test
    @DisplayName("강의 수정 실패 테스트 - 시작 시간 24시간 미만 시 OPEN 변경 불가")
    void updateStatusToOpen_Fail_WhenStartTimeWithin24Hours() {
        LocalDateTime now = LocalDateTime.now();

        Course testCourse2 = new Course(
                1L,
                "Test Title",
                "Test Description",
                150000,
                100,
                50,
                now.minusDays(1),
                now.plusDays(1),
                CourseStatus.DRAFT
        );

        given(courseRepository.findById(1L))
                .willReturn(Optional.of(testCourse2));

        CourseUpdateRequest request = new CourseUpdateRequest(
                null,
                "Test Description",
                250000,
                100,
                now.minusDays(1),
                now.plusDays(1),
                CourseStatus.OPEN
        );

        assertThatThrownBy(() -> courseService.courseUpdate(1L, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_STATUS_UPDATE);
    }
}
