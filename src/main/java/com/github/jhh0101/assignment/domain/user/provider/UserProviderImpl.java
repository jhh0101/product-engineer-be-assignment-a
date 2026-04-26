package com.github.jhh0101.assignment.domain.user.provider;

import com.github.jhh0101.assignment.domain.course.client.user.UserCourseClient;
import com.github.jhh0101.assignment.domain.enrollment.client.user.UserEnrollmentClient;
import com.github.jhh0101.assignment.domain.user.dto.UserInfoResponse;
import com.github.jhh0101.assignment.domain.user.entity.User;
import com.github.jhh0101.assignment.domain.user.repository.UserRepository;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProviderImpl implements UserEnrollmentClient, UserCourseClient {
    private final UserRepository userRepository;

    @Override
    public UserInfoResponse getUserResponse(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserInfoResponse.from(user);
    }

    @Override
    public Map<Long, UserInfoResponse> getUserResponses(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<User> users = userRepository.findAllByIdIn(userIds);

        if (users.isEmpty()) {
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
        }

        return users.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        UserInfoResponse::from
                ));
    }
    @Override
    public UserInfoResponse getUserCourseResponse(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserInfoResponse.from(user);
    }

    @Override
    public Map<Long, UserInfoResponse> getUserCourseResponses(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<User> users = userRepository.findAllByIdIn(userIds);

        if (users.isEmpty()) {
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
        }

        return users.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        UserInfoResponse::from
                ));
    }
}
