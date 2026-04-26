package com.github.jhh0101.assignment.enrollment.service;

import com.github.jhh0101.assignment.config.TestRedisConfig;
import com.github.jhh0101.assignment.domain.course.client.user.UserCourseClient;
import com.github.jhh0101.assignment.domain.course.dto.EnrollmentCancelledEvent;
import com.github.jhh0101.assignment.domain.enrollment.client.course.CourseEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.client.user.UserEnrollmentClient;
import com.github.jhh0101.assignment.domain.user.dto.UserInfoResponse;
import com.github.jhh0101.assignment.domain.enrollment.entity.Enrollment;
import com.github.jhh0101.assignment.domain.enrollment.entity.EnrollmentStatus;
import com.github.jhh0101.assignment.domain.enrollment.repository.EnrollmentRepository;
import com.github.jhh0101.assignment.domain.enrollment.service.EnrollmentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestRedisConfig.class)
public class EnrollmentCancelledServiceIntegrationTest {
    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @MockitoBean
    private CourseEnrollmentClient courseClient;

    @MockitoBean
    private UserEnrollmentClient userClient;

    @MockitoBean
    private UserCourseClient userCourseClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();

        Enrollment enrollment = Enrollment.builder()
                .userId(1L)
                .courseId(1L)
                .status(EnrollmentStatus.CONFIRMED)
                .enrolledAt(LocalDateTime.now())
                .version(0L)
                .build();

        testEnrollment = enrollmentRepository.save(enrollment);

        given(courseClient.getCourseResponse(anyLong()))
                .willReturn(CourseEnrollmentResponse.builder().maxCapacity(40).currentCapacity(0).build());

        given(userClient.getUserResponse(anyLong()))
                .willReturn(UserInfoResponse.builder().name("Test Name").build());

        redisTemplate.opsForValue().set("course:maxCapacity:1", "30");
    }

    @AfterEach
    void tearDown() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("수강 취소 성공 동시성 테스트(낙관적 락) - 5번의 동시 취소 요청 중 1번만 성공해야 함")
    void enrollmentCancelled_version_success() throws InterruptedException {
        Long userId = testEnrollment.getUserId();
        Long enrollmentId = testEnrollment.getId();

        int threadCount = 5;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    enrollmentService.enrollmentCancelled(userId, enrollmentId);
                    successCount.incrementAndGet();
                } catch (ObjectOptimisticLockingFailureException e) {
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("예상치 못한 에러 발생: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(4);

        Enrollment result = enrollmentRepository.findById(enrollmentId).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("수강 취소 성공 테스트 - 결제 확정 7일 이내 수강 취소는 성공해야 함")
    void enrollmentCancelled_redis_consistency_success() {
        Long userId = testEnrollment.getUserId();
        Long enrollmentId = testEnrollment.getId();
        Long courseId = testEnrollment.getCourseId();
        String redisKey = "course:maxCapacity:" + courseId;

        enrollmentService.enrollmentCancelled(userId, enrollmentId);

        String remainingSeats = redisTemplate.opsForValue().get(redisKey);

        assertThat(remainingSeats).isEqualTo("31");

        Enrollment result = enrollmentRepository.findById(enrollmentId).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("대기열 자동 등록 테스트 - 수강 취소 시 대기열 1번 유저가 자동으로 수강 신청되어야 함")
    void auto_enroll_from_waitlist_when_enrollment_cancelled() {
        Long courseId = testEnrollment.getCourseId();
        Long cancelUserId = testEnrollment.getUserId();
        Long cancelEnrollmentId = testEnrollment.getId();
        String waitlistKey = "course:waitlist:" + courseId;
        String capacityKey = "course:maxCapacity:" + courseId;

        redisTemplate.opsForValue().set(capacityKey, "0");

        for (int i = 1; i <= 10; i++) {
            Long waitUserId = 100L + i;
            redisTemplate.opsForZSet().add(waitlistKey, String.valueOf(waitUserId), i);
        }

        assertThat(redisTemplate.opsForZSet().zCard(waitlistKey)).isEqualTo(10);
        Set<String> firstUser = redisTemplate.opsForZSet().range(waitlistKey, 0, 0);
        assertThat(firstUser.iterator().next()).isEqualTo("101");

        enrollmentService.enrollmentCancelled(cancelUserId, cancelEnrollmentId);

        Enrollment cancelledEnrollment = enrollmentRepository.findById(cancelEnrollmentId).orElseThrow();
        assertThat(cancelledEnrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);

        assertThat(redisTemplate.opsForZSet().zCard(waitlistKey)).isEqualTo(9);

        Double waitUserScore = redisTemplate.opsForZSet().score(waitlistKey, "101");
        assertThat(waitUserScore).isNull();

        String currentCapacity = redisTemplate.opsForValue().get(capacityKey);
        assertThat(currentCapacity).isEqualTo("0");

        Enrollment autoEnrolled = enrollmentRepository.findByUserIdAndCourseId(101L, courseId)
                .orElseThrow(() -> new AssertionError("대기열 1번 유저의 수강 신청 데이터가 생성되지 않았습니다."));

        assertThat(autoEnrolled.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
    }
}
