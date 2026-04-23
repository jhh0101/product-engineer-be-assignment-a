package com.github.jhh0101.assignment.course.service;

import com.github.jhh0101.assignment.domain.course.dto.CourseDetailResponse;
import com.github.jhh0101.assignment.domain.course.dto.CourseResponse;
import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.course.repository.CourseListCondition;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;


@ExtendWith(MockitoExtension.class)
public class CourseDetailServiceTest {
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
                "Test DRAFT Title",
                "Test Description",
                150000,
                50,
                0,
                now.plusDays(5),
                now.plusMonths(5),
                CourseStatus.DRAFT
        );

    }

    @Test
    @DisplayName("강의 상세 내용 조회 성공 테스트")
    void courseDetail_success() {
        given(courseRepository.findById(1L))
                .willReturn(Optional.of(testCourse));

        CourseDetailResponse response = courseService.courseDetail(1L);

        then(courseRepository).should(times(1)).findById(1L);

        assertThat(response.getId()).isEqualTo(testCourse.getId());
        assertThat(response.getTitle()).isEqualTo(testCourse.getTitle());
        assertThat(response.getCurrentCapacity()).isEqualTo(testCourse.getCurrentCapacity());
    }

    @Test
    @DisplayName("강의 상세 내용 조회 실패 테스트 - 강의를 찾을 수 없음")
    void courseDetail_not_found() {
        given(courseRepository.findById(1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.courseDetail(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COURSE_NOT_FOUND);
    }
}