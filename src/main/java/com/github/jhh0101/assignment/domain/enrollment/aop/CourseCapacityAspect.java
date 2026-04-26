package com.github.jhh0101.assignment.domain.enrollment.aop;

import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.enrollment.client.course.CourseEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.client.user.UserEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.entity.EnrollmentStatus;
import com.github.jhh0101.assignment.domain.enrollment.error.WaitlistRegisteredException;
import com.github.jhh0101.assignment.domain.enrollment.repository.EnrollmentRepository;
import com.github.jhh0101.assignment.domain.user.dto.UserInfoResponse;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import com.github.jhh0101.assignment.global.util.CustomSpringELParser;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Order(1)
@Aspect
@Component
@RequiredArgsConstructor
public class CourseCapacityAspect {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseEnrollmentClient courseClient;
    private final UserEnrollmentClient userClient;
    private final StringRedisTemplate redisTemplate;

    @Around("@annotation(checkCourseCapacity)")
    public Object checkCapacity(ProceedingJoinPoint joinPoint, CheckCourseCapacity checkCourseCapacity) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Long courseId = CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(),
                joinPoint.getArgs(),
                checkCourseCapacity.key(),
                Long.class
        );

        Long userId = CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(),
                joinPoint.getArgs(),
                checkCourseCapacity.userKey(),
                Long.class
        );

        boolean existsEnrollment = enrollmentRepository.existsByUserIdAndCourseIdAndStatusNot(userId, courseId, EnrollmentStatus.CANCELLED);

        if (existsEnrollment) {
            throw new CustomException(ErrorCode.ALREADY_ENROLLED);
        }

        CourseEnrollmentResponse courseResponse = courseClient.getCourseResponse(courseId);
        UserInfoResponse userResponse = userClient.getUserResponse(userId);

        if (courseResponse.getStatus() != CourseStatus.OPEN) {
            throw new CustomException(ErrorCode.NOT_OPEN_COURSE);
        }
        String redisKey = "course:maxCapacity:" + courseId;

        Long remainCapacity = redisTemplate.opsForValue().decrement(redisKey);

        if (remainCapacity != null && remainCapacity < 0) {
            redisTemplate.opsForValue().increment(redisKey);

            String waitlistKey = "course:waitlist:" + courseId;
            String userIdStr = userId.toString();

            Double existingScore = redisTemplate.opsForZSet().score(waitlistKey, userIdStr);

            if (existingScore == null) {
                redisTemplate.opsForZSet().add(waitlistKey, userIdStr, System.currentTimeMillis());
            }

            Long rank = redisTemplate.opsForZSet().rank(waitlistKey, userIdStr);
            long currentWaitNumber = (rank != null ? rank : 0) + 1;

            Long totalCount = redisTemplate.opsForZSet().zCard(waitlistKey);
            long waitTotalCount = (totalCount != null ? totalCount : 0);

            throw new WaitlistRegisteredException(userId,userResponse.getName(), courseResponse.getTitle(), currentWaitNumber, waitTotalCount);
        }

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            redisTemplate.opsForValue().increment(redisKey);
            throw e;
        }
    }
}
