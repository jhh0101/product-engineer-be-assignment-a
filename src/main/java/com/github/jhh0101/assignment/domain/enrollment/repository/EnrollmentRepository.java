package com.github.jhh0101.assignment.domain.enrollment.repository;

import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.enrollment.entity.Enrollment;
import com.github.jhh0101.assignment.domain.enrollment.entity.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    Page<Enrollment> findAllByUserId(Long userId, Pageable pageable);

    Page<Enrollment> findAllByCourseId(Long courseId, Pageable pageable);

    boolean existsByUserIdAndCourseIdAndStatusNot(Long userId, Long courseId, EnrollmentStatus status);
}
