package com.huuimcm.assignment.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record UserCreateRequest(

        @Schema(description = "로그인 ID", example = "john123")
        @NotBlank(message = "로그인 ID는 필수입니다")
        @Size(max = 50, message = "로그인 ID는 50자 이하여야 합니다")
        String loginId,

        @Schema(description = "비밀번호", example = "password123")
        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(max = 255, message = "비밀번호는 255자 이하여야 합니다")
        String loginPw,

        @Schema(description = "이름", example = "홍길동")
        @NotBlank(message = "이름은 필수입니다")
        @Size(max = 50, message = "이름은 50자 이하여야 합니다")
        String name
) {}
