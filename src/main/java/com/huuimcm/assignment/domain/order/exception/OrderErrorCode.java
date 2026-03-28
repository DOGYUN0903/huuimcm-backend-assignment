package com.huuimcm.assignment.domain.order.exception;

import com.huuimcm.assignment.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    DUPLICATE_ORDER(HttpStatus.CONFLICT, "이미 처리된 주문입니다");

    private final HttpStatus status;
    private final String message;
}
