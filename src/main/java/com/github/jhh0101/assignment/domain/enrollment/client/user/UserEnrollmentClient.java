package com.github.jhh0101.assignment.domain.enrollment.client.user;

import com.github.jhh0101.assignment.domain.user.dto.UserInfoResponse;

import java.util.List;
import java.util.Map;

public interface UserEnrollmentClient {
    UserInfoResponse getUserResponse(Long userId);
    Map<Long, UserInfoResponse> getUserResponses(List<Long> userIds);
}
