package com.github.jhh0101.assignment.domain.course.repository;

import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;

import static com.github.jhh0101.assignment.domain.course.entity.QCourse.course;

@Repository
@RequiredArgsConstructor
public class CourseRepositoryCustomImpl implements CourseRepositoryCustom{
    private final JPAQueryFactory jpaQueryFactory;

    public Page<Course> courseListSearch(CourseListCondition condition, LocalDateTime now, Pageable pageable) {
        List<Course> content = jpaQueryFactory
                .selectFrom(course)
                .where(
                        statusEq(condition.status(), now)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(course.id.desc())
                .fetch();

        long total = jpaQueryFactory
                .select(course.count())
                .from(course)
                .where(statusEq(condition.status(), now))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);

    }

    private BooleanExpression statusEq(String status, LocalDateTime now) {
        CourseStatus searchStatus = (status != null) ? CourseStatus.valueOf(status.toUpperCase()) : null;

        if (searchStatus == null) return null;

        if (searchStatus == CourseStatus.CLOSED) {
            return course.status.eq(CourseStatus.CLOSED)
                    .or(course.status.eq(CourseStatus.OPEN).and(course.startTime.before(now.plusDays(1))));
        }

        if (searchStatus == CourseStatus.OPEN) {
            return course.status.eq(CourseStatus.OPEN).and(course.startTime.goe(now.plusDays(1)));
        }

        return course.status.eq(searchStatus);
    }
}
