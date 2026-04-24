package com.github.jhh0101.assignment.domain.enrollment.client.user;

import com.github.jhh0101.assignment.domain.enrollment.client.user.dto.UserEnrollmentResponse;

import java.util.List;
import java.util.Map;

public interface UserEnrollmentClient {
    UserEnrollmentResponse getUserResponse(Long userId);
    Map<Long, UserEnrollmentResponse> getUserResponses(List<Long> userIds);
}
