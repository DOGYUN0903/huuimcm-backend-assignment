package com.huuimcm.assignment.domain.user.controller;

import com.huuimcm.assignment.domain.user.dto.UserCreateRequest;
import com.huuimcm.assignment.domain.user.dto.UserCreateResponse;
import com.huuimcm.assignment.domain.user.service.UserService;
import com.huuimcm.assignment.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserCreateResponse>> signup(
            @Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.success(HttpStatus.CREATED, "회원가입이 완료되었습니다", userService.signup(request));
    }
}
