package com.github.jhh0101.assignment.domain.enrollment.client.user.dto;

import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEnrollmentResponse {
    private Long id;
    private String name;


    public static UserEnrollmentResponse from(User entity){
        return UserEnrollmentResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
