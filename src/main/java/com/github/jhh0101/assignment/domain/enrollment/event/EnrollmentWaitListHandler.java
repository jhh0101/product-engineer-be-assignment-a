package com.github.jhh0101.assignment.domain.enrollment.event;

import com.github.jhh0101.assignment.domain.course.dto.CourseClosedEvent;
import com.github.jhh0101.assignment.domain.course.dto.CourseOpenedEvent;
import com.github.jhh0101.assignment.domain.course.dto.EnrollmentCancelledEvent;
import com.github.jhh0101.assignment.domain.enrollment.client.course.CourseEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.entity.Enrollment;
import com.github.jhh0101.assignment.domain.enrollment.entity.EnrollmentStatus;
import com.github.jhh0101.assignment.domain.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentWaitListHandler {
    private final EnrollmentRepository enrollmentRepository;
    private final StringRedisTemplate redisTemplate;
    private final CourseEnrollmentClient courseClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleEnrollmentCancelled(EnrollmentCancelledEvent event) {
        Long courseId = event.courseId();
        String waitlistKey = "course:waitlist:" + courseId;
        String capacityKey = "course:maxCapacity:" + courseId;

        ZSetOperations.TypedTuple<String> nextUser = redisTemplate.opsForZSet().popMin(waitlistKey);

        if (nextUser != null && nextUser.getValue() != null) {
            Long nextUserId = Long.parseLong(nextUser.getValue());

            Enrollment promoted = enrollmentRepository.findByUserIdAndCourseId(nextUserId, courseId)
                    .orElse(null);

            if (promoted != null) {
                promoted.reEnroll();
            } else {
                promoted = Enrollment.builder()
                        .userId(nextUserId)
                        .courseId(courseId)
                        .status(EnrollmentStatus.PENDING)
                        .build();
            }
            enrollmentRepository.save(promoted);

            redisTemplate.opsForValue().decrement(capacityKey);

            log.info("대기열 유저 승격 성공: {}", nextUserId);

        } else {
            courseClient.subStudent(courseId);
        }
    }
}