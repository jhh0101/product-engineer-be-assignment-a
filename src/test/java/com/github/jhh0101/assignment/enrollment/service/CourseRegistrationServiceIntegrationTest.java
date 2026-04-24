package com.github.jhh0101.assignment.enrollment.service;

import com.github.jhh0101.assignment.config.TestRedisConfig;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.enrollment.client.course.CourseEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.client.user.UserEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.user.dto.UserEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentRegistrationResponse;
import com.github.jhh0101.assignment.domain.enrollment.entity.Enrollment;
import com.github.jhh0101.assignment.domain.enrollment.entity.EnrollmentStatus;
import com.github.jhh0101.assignment.domain.enrollment.repository.EnrollmentRepository;
import com.github.jhh0101.assignment.domain.enrollment.service.EnrollmentService;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(TestRedisConfig.class)
public class CourseRegistrationServiceIntegrationTest {
    @Autowired
    private EnrollmentService enrollmentService;

    @MockitoBean
    private EnrollmentRepository enrollmentRepository;

    @MockitoBean
    private CourseEnrollmentClient courseClient;

    @MockitoBean
    private UserEnrollmentClient userClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void tearDown() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("수강 신청 성공 동시성 테스트 - 100명이 동시에 신청하면 정원만큼만 성공")
    void courseRegistration_concurrency_shouldSucceedExactlyUpToCapacity() throws InterruptedException {
        Long courseId = 1L;

        given(courseClient.getCourseResponse(courseId))
                .willReturn(CourseEnrollmentResponse.builder().maxCapacity(30).status(CourseStatus.OPEN).build());

        int totalRequests = 100;

        redisTemplate.opsForValue().set("course:maxCapacity:" + courseId, String.valueOf(30));

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(totalRequests);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        given(userClient.getUserResponse(anyLong()))
                .willReturn(UserEnrollmentResponse.builder().name("테스트유저").build());

        for (int i = 1; i <= totalRequests; i++) {
            Long userId = (long) i;

            executorService.submit(() -> {
                try {
                    enrollmentService.courseRegistration(userId, courseId);
                    successCount.getAndIncrement();
                } catch (Exception e) {
                    failCount.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        assertThat(successCount.get()).isEqualTo(30);
        assertThat(failCount.get()).isEqualTo(totalRequests - 30);

        verify(courseClient, times(30)).addStudent(courseId);
    }

    @Test
    @DisplayName("수강 신청 실패 테스트 - 신청 에러 시 수강 신청 카운트 Roll Back 테스트")
    void courseRegistration_fail_rollback() {
        Long courseId = 1L;
        Long userId = 1L;
        String key = "course:maxCapacity:" + courseId;

        Enrollment enrollment = new Enrollment(
                1L,
                userId,
                courseId,
                EnrollmentStatus.CONFIRMED,
                null,
                1L
        );

        given(courseClient.getCourseResponse(courseId))
                .willReturn(CourseEnrollmentResponse.builder().maxCapacity(30).status(CourseStatus.OPEN).build());

        given(enrollmentRepository.findByUserIdAndCourseId(userId, courseId))
                .willReturn(Optional.of(enrollment));

        given(userClient.getUserResponse(anyLong()))
                .willReturn(UserEnrollmentResponse.builder().name("테스트유저").build());

        redisTemplate.opsForValue().set(key, "30");

        assertThatThrownBy(() -> enrollmentService.courseRegistration(userId, courseId))
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_ENROLLED);

        String currentCapacity = redisTemplate.opsForValue().get(key);
        assertThat(currentCapacity).isEqualTo("30");

        verify(courseClient, times(0)).addStudent(courseId);
    }

    @ParameterizedTest
    @EnumSource(value = CourseStatus.class, names = {"DRAFT", "CLOSED"})
    @DisplayName("수강 신청 실패 테스트 - 강의 상태가 Open이 아닌 경우 에러 테스트")
    void courseRegistration_course_status_not_open(CourseStatus status) {
        Long courseId = 1L;
        Long userId = 1L;
        String key = "course:maxCapacity:" + courseId;

        given(courseClient.getCourseResponse(courseId))
                .willReturn(CourseEnrollmentResponse.builder().maxCapacity(30).status(status).build());

        redisTemplate.opsForValue().set(key, "30");

        assertThatThrownBy(() -> enrollmentService.courseRegistration(userId, courseId))
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_OPEN_COURSE);

        String currentCapacity = redisTemplate.opsForValue().get(key);
        assertThat(currentCapacity).isEqualTo("30");

        verify(courseClient, times(0)).addStudent(courseId);
    }
}
