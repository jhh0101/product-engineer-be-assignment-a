package com.github.jhh0101.assignment.domain.user.dto;

import com.github.jhh0101.assignment.domain.user.entity.Role;
import com.github.jhh0101.assignment.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoResponse {
    private Long id;
    private String name;
    private Role role;


    public static UserInfoResponse from(User entity){
        return UserInfoResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .role(entity.getRole())
                .build();
    }
}
