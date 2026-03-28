package com.huuimcm.assignment.domain.user.exception;

import com.huuimcm.assignment.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 존재하는 로그인 ID입니다");

    private final HttpStatus status;
    private final String message;
}
