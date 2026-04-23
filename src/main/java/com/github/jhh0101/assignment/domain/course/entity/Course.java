package com.github.jhh0101.assignment.domain.course.entity;

import com.github.jhh0101.assignment.domain.course.dto.CourseUpdateRequest;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private Integer price;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "current_capacity")
    private Integer currentCapacity;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    public void courseUpdate(CourseUpdateRequest request) {
        if (this.status == CourseStatus.CLOSED) {
            throw new CustomException(ErrorCode.COURSE_STATUS_CHANGE_ERROR);
        }
        if (StringUtils.hasText(request.getTitle())) {
            this.title = request.getTitle();
        }
        if (StringUtils.hasText(request.getDescription())) {
            this.description = request.getDescription();
        }
        if (request.getPrice() != null && request.getPrice() >= 0) {
            this.price = request.getPrice();
        }
        if (request.getMaxCapacity() != null) {
            if (request.getMaxCapacity() < this.currentCapacity) {
                throw new CustomException(ErrorCode.INVALID_CAPACITY_UPDATE);
            }
            if (request.getMaxCapacity() > 0) {
                this.maxCapacity = request.getMaxCapacity();
            }
        }
        if (request.getStartTime() != null) {
            this.startTime = request.getStartTime();
        }
        if (request.getEndTime() != null) {
            this.endTime = request.getEndTime();
        }
        if (request.getStatus() != null) {
            this.status = request.getStatus();
        }

        if (this.startTime.isAfter(this.endTime)) {
            throw new CustomException(ErrorCode.COURSE_INVALID_PERIOD);
        }

        if (request.getStatus() == CourseStatus.OPEN && this.startTime.minusDays(1).isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.INVALID_STATUS_UPDATE);
        }
    }

    public void courseClose() {
        this.status = CourseStatus.CLOSED;
    }

    public void addStudent() {
        this.currentCapacity++;
    }
}
