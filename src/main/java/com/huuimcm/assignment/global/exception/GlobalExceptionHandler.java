package com.huuimcm.assignment.global.exception;

import com.huuimcm.assignment.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 커스텀 오류로 발생한 커스텀 예외 처리
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("비즈니스 예외 발생 - 코드: {}, 메시지: {}", e.getErrorCode().getStatus(), e.getMessage());
        return ApiResponse.error(e.getErrorCode().getStatus(), e.getMessage());
    }

    // 2. Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("잘못된 요청입니다.");

        log.warn("유효성 검증 실패 - 메시지: {}", errorMessage);
        return ApiResponse.error(HttpStatus.BAD_REQUEST, errorMessage);
    }

    // 3. 그 외 모든 예외 처리 (최후의 보루)
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("처리되지 않은 예외 발생", e);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내에서 오류가 발생하였습니다.");
    }
}
