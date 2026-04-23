package com.github.jhh0101.assignment.domain.enrollment.dto;

import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.client.user.dto.UserEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.entity.Enrollment;
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
public class EnrollmentConfirmedResponse {
    private Long id;
    private String name;
    private String title;
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;

    public static EnrollmentConfirmedResponse from(Enrollment entity, UserEnrollmentResponse userResponse, CourseEnrollmentResponse courseResponse) {
        return EnrollmentConfirmedResponse.builder()
                .id(entity.getId())
                .name(userResponse.getName())
                .title(courseResponse.getTitle())
                .status(entity.getStatus())
                .enrolledAt(entity.getEnrolledAt())
                .build();
    }
}
