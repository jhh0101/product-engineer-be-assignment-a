package com.github.jhh0101.assignment.domain.course.service;

import com.github.jhh0101.assignment.domain.course.client.user.UserCourseClient;
import com.github.jhh0101.assignment.domain.course.dto.*;
import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.course.repository.CourseListCondition;
import com.github.jhh0101.assignment.domain.course.repository.CourseRepository;
import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentListResponse;
import com.github.jhh0101.assignment.domain.enrollment.entity.Enrollment;
import com.github.jhh0101.assignment.domain.user.dto.UserInfoResponse;
import com.github.jhh0101.assignment.domain.user.entity.Role;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserCourseClient userCourseClient;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CourseResponse courseCreate(Long userId, CourseCreateRequest request) {
        UserInfoResponse userResponse = userCourseClient.getUserCourseResponse(userId);
        if (userResponse.getRole() != Role.CREATOR) {
            throw new CustomException(ErrorCode.USER_NOT_CREATOR);
        }
        Course course = Course.builder()
                .creatorId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .maxCapacity(request.getMaxCapacity())
                .currentCapacity(0)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(CourseStatus.DRAFT)
                .build();

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new CustomException(ErrorCode.COURSE_INVALID_PERIOD);
        }

        return CourseResponse.from(courseRepository.save(course), userResponse);
    }

    @Transactional
    public CourseResponse courseUpdate(Long userId, Long courseId, CourseUpdateRequest request) {
        UserInfoResponse userResponse = userCourseClient.getUserCourseResponse(userId);
        if (userResponse.getRole() != Role.CREATOR) {
            throw new CustomException(ErrorCode.USER_NOT_CREATOR);
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));

        if (!course.getCreatorId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_COURSE_OWNER);
        }

        course.courseUpdate(request);

        if (course.getStatus() == CourseStatus.OPEN) {
            eventPublisher.publishEvent(new CourseOpenedEvent(courseId, course.getMaxCapacity() - course.getCurrentCapacity()));
        } else {
            eventPublisher.publishEvent(new CourseClosedEvent(courseId));
        }

        return CourseResponse.from(course, userResponse);
    }

    @Transactional
    public Page<CourseResponse> courseList(CourseListCondition condition, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();

        Page<Course> courses = courseRepository.courseListSearch(condition, now, pageable);

        courses.getContent().forEach(course -> {
            if (course.getStatus() == CourseStatus.OPEN && course.getStartTime().minusDays(1).isBefore(now)) {
                course.courseClose();
            }
        });

        List<Long> userIds = courses.stream()
                .map(Course::getCreatorId)
                .distinct()
                .toList();

        Map<Long, UserInfoResponse> userMap = userCourseClient.getUserCourseResponses(userIds);

        return courses.map(course -> {
            UserInfoResponse userMapResponse = userMap.get(course.getCreatorId());
            return CourseResponse.from(course, userMapResponse);
        });
    }

    public CourseDetailResponse courseDetail(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));

        UserInfoResponse userResponse = userCourseClient.getUserCourseResponse(course.getCreatorId());

        return CourseDetailResponse.from(course, userResponse);
    }
}
