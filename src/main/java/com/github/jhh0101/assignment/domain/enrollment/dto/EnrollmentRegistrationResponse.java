package com.github.jhh0101.assignment.domain.enrollment.dto;

import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.client.user.dto.UserEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.entity.Enrollment;
import com.github.jhh0101.assignment.domain.enrollment.entity.EnrollmentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentRegistrationResponse {
    private Long id;
    private String name;
    private String title;
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;

    public static EnrollmentRegistrationResponse from(Enrollment entity, UserEnrollmentResponse userResponse, CourseEnrollmentResponse courseResponse) {
        return EnrollmentRegistrationResponse.builder()
                .id(entity.getId())
                .name(userResponse.getName())
                .title(courseResponse.getTitle())
                .status(entity.getStatus())
                .enrolledAt(entity.getEnrolledAt())
                .build();
    }
}
