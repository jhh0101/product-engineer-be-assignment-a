package com.github.jhh0101.assignment.domain.enrollment.dto;

import com.github.jhh0101.assignment.domain.enrollment.entity.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitlistResponse {
    private Long id;
    private String name;
    private String title;
    private Long  waitNumber;
    private Long totalWaitingCount;
}
