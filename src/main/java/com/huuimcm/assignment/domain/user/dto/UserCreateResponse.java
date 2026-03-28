package com.huuimcm.assignment.domain.user.dto;

import com.huuimcm.assignment.domain.user.entity.User;

public record UserCreateResponse(
        Long id,
        String loginId,
        String name
) {
    public static UserCreateResponse from(User user) {
        return new UserCreateResponse(user.getId(), user.getLoginId(), user.getName());
    }
}
