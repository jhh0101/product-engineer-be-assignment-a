package com.github.jhh0101.assignment.domain.course.dto;

import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private Integer price;
    private Integer maxCapacity;
    private Integer currentCapacity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private CourseStatus status;

    public static CourseResponse from(Course entity){
        return CourseResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .maxCapacity(entity.getMaxCapacity())
                .currentCapacity(entity.getCurrentCapacity())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .status(entity.getStatus())
                .build();
    }
}

