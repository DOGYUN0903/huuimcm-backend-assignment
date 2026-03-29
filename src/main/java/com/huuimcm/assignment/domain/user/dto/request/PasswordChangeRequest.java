package com.huuimcm.assignment.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 변경 요청")
public record PasswordChangeRequest(

        @Schema(description = "새 비밀번호", example = "newPassword456")
        @NotBlank(message = "새 비밀번호는 필수입니다")
        String newPassword
) {}
