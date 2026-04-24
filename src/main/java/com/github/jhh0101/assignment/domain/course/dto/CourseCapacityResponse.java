package com.github.jhh0101.assignment.domain.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCapacityResponse {
    private Long courseId;
    private Integer availableSeats;
}
