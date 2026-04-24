package com.github.jhh0101.assignment.course.service;

import com.github.jhh0101.assignment.config.TestRedisConfig;
import com.github.jhh0101.assignment.domain.course.client.user.UserCourseClient;
import com.github.jhh0101.assignment.domain.course.dto.CourseUpdateRequest;
import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.course.repository.CourseRepository;
import com.github.jhh0101.assignment.domain.course.service.CourseService;
import com.github.jhh0101.assignment.domain.enrollment.client.user.UserEnrollmentClient;
import com.github.jhh0101.assignment.domain.user.dto.UserInfoResponse;
import com.github.jhh0101.assignment.domain.user.entity.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Import(TestRedisConfig.class)
public class CourseUpdateServiceIntegrationTest {
    @Autowired
    private CourseService courseService;

    @MockitoBean
    private CourseRepository courseRepository;

    @MockitoBean
    private UserCourseClient userCourseClient;

    @MockitoBean
    private UserEnrollmentClient userEnrollmentClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void tearDown() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("강의 상태를 OPEN으로 변경하면 레디스에 최대 정원이 저장 / 수강 신청자가 있는 경우 신청자의 수를 제외한 최대 인원수를 레디스에 저장")
    void courseUpdate_course_status_open() throws InterruptedException {
        LocalDateTime now = LocalDateTime.now();

        Course testCourse = new Course(
                1L,
                1L,
                "Test Title",
                "Test Description",
                150000,
                50,
                20,
                now.plusDays(5),
                now.plusMonths(5),
                CourseStatus.DRAFT
        );

        given(courseRepository.findById(1L))
                .willReturn(Optional.of(testCourse));

        CourseUpdateRequest request = new CourseUpdateRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                CourseStatus.OPEN
        );

        given(userCourseClient.getUserCourseResponse(anyLong()))
                .willReturn(UserInfoResponse.builder().role(Role.CREATOR).name("Test Name").build());

        courseService.courseUpdate(testCourse.getCreatorId(), testCourse.getId(), request);

        String redisKey = "course:maxCapacity:" + testCourse.getId();
        String savedCapacity = redisTemplate.opsForValue().get(redisKey);

        assertThat(savedCapacity).isNotNull();
        assertThat(Integer.parseInt(savedCapacity)).isEqualTo(testCourse.getMaxCapacity() - testCourse.getCurrentCapacity());
    }

    @Test
    @DisplayName("강의 상태를 OPEN으로 변경하는게 아니라면 레디스에 저장하지 않음")
    void courseUpdate_course_status_not_open() throws InterruptedException {
        LocalDateTime now = LocalDateTime.now();

        Course testCourse = new Course(
                1L,
                1L,
                "Test Title",
                "Test Description",
                150000,
                50,
                20,
                now.plusDays(5),
                now.plusMonths(5),
                CourseStatus.DRAFT
        );

        given(courseRepository.findById(1L))
                .willReturn(Optional.of(testCourse));

        CourseUpdateRequest request = new CourseUpdateRequest(
                "Test New Title",
                null,
                null,
                null,
                null,
                null,
                null
        );

        given(userCourseClient.getUserCourseResponse(anyLong()))
                .willReturn(UserInfoResponse.builder().role(Role.CREATOR).name("Test Name").build());

        courseService.courseUpdate(testCourse.getCreatorId(), testCourse.getId(), request);

        String redisKey = "course:maxCapacity:" + testCourse.getId();
        String savedCapacity = redisTemplate.opsForValue().get(redisKey);

        assertThat(savedCapacity).isNull();
    }

    @Test
    @DisplayName("강의 상태를 OPEN으로 변경하는게 아니라면 레디스에 저장하지 않음")
    void courseUpdate_course_status_end_open() throws InterruptedException {
        LocalDateTime now = LocalDateTime.now();

        Course testCourse = new Course(
                1L,
                1L,
                "Test Title",
                "Test Description",
                150000,
                50,
                20,
                now.plusDays(5),
                now.plusMonths(5),
                CourseStatus.OPEN
        );

        given(courseRepository.findById(1L))
                .willReturn(Optional.of(testCourse));

        CourseUpdateRequest request = new CourseUpdateRequest(
                "Test New Title",
                null,
                null,
                null,
                null,
                null,
                CourseStatus.CLOSED
        );

        given(userCourseClient.getUserCourseResponse(anyLong()))
                .willReturn(UserInfoResponse.builder().role(Role.CREATOR).name("Test Name").build());

        String redisKey = "course:maxCapacity:" + testCourse.getId();
        redisTemplate.opsForValue().set(redisKey, String.valueOf(testCourse.getMaxCapacity() - testCourse.getCurrentCapacity()));

        courseService.courseUpdate(testCourse.getCreatorId(), testCourse.getId(), request);

        String closedCapacity = redisTemplate.opsForValue().get(redisKey);

        assertThat(closedCapacity).isNull();
    }
}