package com.github.jhh0101.assignment.domain.course.repository;

import com.github.jhh0101.assignment.domain.course.dto.CourseCapacityResponse;
import com.github.jhh0101.assignment.domain.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, CourseRepositoryCustom {
    @Query("SELECT new com.github.jhh0101.assignment.domain.course.dto.CourseCapacityResponse(c.id, (c.maxCapacity - c.currentCapacity)) " +
            "FROM Course c " +
            "WHERE c.status = 'OPEN'") // 오픈된 강의만 가져오는 조건 추가 추천
    List<CourseCapacityResponse> findAllActiveAvailableSeats();
}
