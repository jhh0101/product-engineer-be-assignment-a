package com.github.jhh0101.assignment.domain.course.repository;

import com.github.jhh0101.assignment.domain.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface CourseRepositoryCustom {
    Page<Course> courseListSearch(CourseListCondition condition, LocalDateTime now, Pageable pageable);
}
