package com.github.jhh0101.assignment.domain.enrollment.aop;

import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.enrollment.client.course.CourseEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
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
    private final StringRedisTemplate redisTemplate;
    private final CourseEnrollmentClient courseClient;

    @Around("@annotation(checkCourseCapacity)")
    public Object checkCapacity(ProceedingJoinPoint joinPoint, CheckCourseCapacity checkCourseCapacity) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Long courseId = CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(),
                joinPoint.getArgs(),
                checkCourseCapacity.key(),
                Long.class
        );

        CourseEnrollmentResponse courseResponse = courseClient.getCourseResponse(courseId);
        if (courseResponse.getStatus() != CourseStatus.OPEN) {
            throw new CustomException(ErrorCode.NOT_OPEN_COURSE);
        }
        String redisKey = "course:maxCapacity:" + courseId;

        Long remainCapacity = redisTemplate.opsForValue().decrement(redisKey);

        if (remainCapacity != null && remainCapacity < 0) {
            redisTemplate.opsForValue().increment(redisKey);
            throw new CustomException(ErrorCode.CAPACITY_EXCEEDED);
        }

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            redisTemplate.opsForValue().increment(redisKey);
            throw e;
        }
    }
}
