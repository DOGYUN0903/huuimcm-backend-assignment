package com.huuimcm.assignment.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequest(

        @NotBlank(message = "새 비밀번호는 필수입니다")
        String newPassword
) {}
