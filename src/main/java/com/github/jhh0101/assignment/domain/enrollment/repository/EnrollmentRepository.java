package com.github.jhh0101.assignment.domain.enrollment.repository;

import com.github.jhh0101.assignment.domain.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Course, Long> {
}
