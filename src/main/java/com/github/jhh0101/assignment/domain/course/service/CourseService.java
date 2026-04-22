package com.github.jhh0101.assignment.domain.course.service;

import com.github.jhh0101.assignment.domain.course.dto.CourseRequest;
import com.github.jhh0101.assignment.domain.course.dto.CourseResponse;
import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.course.repository.CourseRepository;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {
    private final CourseRepository courseRepository;

    public CourseResponse courseCreate(CourseRequest request) {
        Course course = Course.builder()
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

        return CourseResponse.from(courseRepository.save(course));
    }
}
