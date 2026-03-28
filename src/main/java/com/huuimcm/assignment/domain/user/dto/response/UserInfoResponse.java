package com.huuimcm.assignment.domain.user.dto.response;

import com.huuimcm.assignment.domain.user.entity.User;

import java.time.LocalDateTime;

public record UserInfoResponse(
        Long id,
        String loginId,
        String name,
        LocalDateTime createdAt
) {
    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(user.getId(), user.getLoginId(), user.getName(), user.getCreatedAt());
    }
}
