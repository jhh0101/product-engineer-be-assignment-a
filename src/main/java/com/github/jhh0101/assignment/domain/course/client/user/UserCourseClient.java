package com.github.jhh0101.assignment.domain.course.client.user;

import com.github.jhh0101.assignment.domain.user.dto.UserInfoResponse;

import java.util.List;
import java.util.Map;

public interface UserCourseClient {
    UserInfoResponse getUserCourseResponse(Long userId);
    Map<Long, UserInfoResponse> getUserCourseResponses(List<Long> userIds);
}
