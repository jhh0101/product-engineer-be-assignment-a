package com.github.jhh0101.assignment.course.service;

import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.course.repository.CourseListCondition;
import com.github.jhh0101.assignment.domain.course.repository.CourseRepository;
import com.github.jhh0101.assignment.domain.course.service.CourseService;
import com.github.jhh0101.assignment.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Import(QuerydslConfig.class)
public class CourseRepositoryIntegrationTest {
    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("DRAFT 상태로 검색하면 DRAFT 강의만 조회되어야 한다")
    void search_Draft_Test() {
        LocalDateTime now = LocalDateTime.now();

        Course testCourseDraft = new Course(
                null,
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
        Course testCourseOpen = new Course(
                null,
                1L,
                "Test OPEN Title",
                "Test Description",
                150000,
                50,
                0,
                now.plusDays(5),
                now.plusMonths(5),
                CourseStatus.OPEN
        );
        courseRepository.save(testCourseDraft);
        courseRepository.save(testCourseOpen);

        CourseListCondition condition = new CourseListCondition(CourseStatus.DRAFT.name());
        Page<Course> result = courseRepository.courseListSearch(condition, LocalDateTime.now(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test DRAFT Title");
    }

    @Test
    @DisplayName("OPEN 상태로 검색하면 OPEN 강의만 조회되어야 한다")
    void search_Open_Test() {
        LocalDateTime now = LocalDateTime.now();

        Course testCourseDraft = new Course(
                null,
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
        Course testCourseOpen = new Course(
                null,
                1L,
                "Test OPEN Title",
                "Test Description",
                150000,
                50,
                0,
                now.plusDays(5),
                now.plusMonths(5),
                CourseStatus.OPEN
        );

        courseRepository.save(testCourseDraft);
        courseRepository.save(testCourseOpen);

        CourseListCondition condition = new CourseListCondition(CourseStatus.OPEN.name());
        Page<Course> result = courseRepository.courseListSearch(condition, LocalDateTime.now(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test OPEN Title");
    }

    @Test
    @DisplayName("CLOSED 상태로 검색하면 CLOSED 강의만 조회되어야 한다")
    void search_Closed_Test() {
        LocalDateTime now = LocalDateTime.now();

        Course testCourseDraft = new Course(
                null,
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
        Course testCourseOpen = new Course(
                null,
                1L,
                "Test CLOSED Title",
                "Test Description",
                150000,
                50,
                0,
                now.minusDays(1),
                now.plusMonths(5),
                CourseStatus.OPEN
        );
        Course testCourseClosed = new Course(
                null,
                1L,
                "Test CLOSED Title",
                "Test Description",
                150000,
                50,
                0,
                now.minusDays(5),
                now.plusMonths(5),
                CourseStatus.OPEN
        );

        courseRepository.save(testCourseDraft);
        courseRepository.save(testCourseOpen);
        courseRepository.save(testCourseClosed);

        CourseListCondition condition = new CourseListCondition(CourseStatus.CLOSED.name());
        Page<Course> result = courseRepository.courseListSearch(condition, LocalDateTime.now(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test CLOSED Title");
    }

}
