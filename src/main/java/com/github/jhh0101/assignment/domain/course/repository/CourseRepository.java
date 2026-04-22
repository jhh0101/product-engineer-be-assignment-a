package com.github.jhh0101.assignment.domain.course.repository;

import com.github.jhh0101.assignment.domain.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, CourseRepositoryCustom {

}
