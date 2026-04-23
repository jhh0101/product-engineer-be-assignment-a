package com.github.jhh0101.assignment.domain.enrollment.client.user;

import com.github.jhh0101.assignment.domain.enrollment.client.user.dto.UserEnrollmentResponse;

public interface UserEnrollmentClient {
    UserEnrollmentResponse getUserResponse(Long userId);
}
