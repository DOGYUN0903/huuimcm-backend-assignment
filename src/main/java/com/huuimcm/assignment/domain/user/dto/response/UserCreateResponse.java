package com.huuimcm.assignment.domain.user.dto.response;

import com.huuimcm.assignment.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 응답")
public record UserCreateResponse(
        @Schema(description = "유저 ID", example = "1")
        Long id,
        @Schema(description = "로그인 ID", example = "john123")
        String loginId,
        @Schema(description = "이름", example = "홍길동")
        String name
) {
    public static UserCreateResponse from(User user) {
        return new UserCreateResponse(user.getId(), user.getLoginId(), user.getName());
    }
}
