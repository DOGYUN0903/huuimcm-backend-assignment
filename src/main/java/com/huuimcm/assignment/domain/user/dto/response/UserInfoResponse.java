package com.huuimcm.assignment.domain.user.dto.response;

import com.huuimcm.assignment.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "내 정보 조회 응답")
public record UserInfoResponse(
        @Schema(description = "유저 ID", example = "1")
        Long id,
        @Schema(description = "로그인 ID", example = "john123")
        String loginId,
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "가입일시", example = "2026-03-29T10:30:00")
        LocalDateTime createdAt
) {
    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(user.getId(), user.getLoginId(), user.getName(), user.getCreatedAt());
    }
}
