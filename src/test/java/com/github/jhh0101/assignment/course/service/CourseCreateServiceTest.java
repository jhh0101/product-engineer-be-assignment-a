package com.github.jhh0101.assignment.course.service;

import com.github.jhh0101.assignment.domain.course.dto.CourseCreateRequest;
import com.github.jhh0101.assignment.domain.course.dto.CourseResponse;
import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.course.repository.CourseRepository;
import com.github.jhh0101.assignment.domain.course.service.CourseService;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;


@ExtendWith(MockitoExtension.class)
public class CourseCreateServiceTest {
    @Mock
    CourseRepository courseRepository;

    @InjectMocks
    CourseService courseService;

    @Test
    @DisplayName("강의 등록 성공 테스트")
    void courseCreate_success() {
        LocalDateTime now = LocalDateTime.now();

        CourseCreateRequest request = new CourseCreateRequest(
                "Test Title",
                "Test Description",
                150000,
                50,
                now,
                now.plusMonths(5)
        );

        Course savedCourse = new Course(
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

        given(courseRepository.save(any(Course.class))).willReturn(savedCourse);

        CourseResponse response = courseService.courseCreate(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        assertThat(response.getStatus()).isEqualTo(CourseStatus.DRAFT);

        then(courseRepository).should(times(1)).save(any(Course.class));
    }

    @Test
    @DisplayName("강의 등록 실패 테스트 - 종료일이 시작일보다 빠름")
    void courseCreate_startTime_Period() {
        LocalDateTime now = LocalDateTime.now();

        CourseCreateRequest request = new CourseCreateRequest(
                "Test Title",
                "Test Description",
                150000,
                50,
                now,
                now.minusDays(5)
        );

        assertThatThrownBy(() -> courseService.courseCreate(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COURSE_INVALID_PERIOD);
    }
}
