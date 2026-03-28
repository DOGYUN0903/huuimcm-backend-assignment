package com.huuimcm.assignment.domain.user.controller;

import com.huuimcm.assignment.domain.user.dto.request.PasswordChangeRequest;
import com.huuimcm.assignment.domain.user.dto.request.UserCreateRequest;
import com.huuimcm.assignment.domain.user.dto.response.UserCreateResponse;
import com.huuimcm.assignment.domain.user.dto.response.UserInfoResponse;
import com.huuimcm.assignment.domain.user.service.UserService;
import com.huuimcm.assignment.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyInfo(
            @RequestHeader("X-Huuim-LoginId") String loginId,
            @RequestHeader("X-Huuim-LoginPw") String loginPw) {
        return ApiResponse.success(HttpStatus.OK, "내 정보 조회 성공", userService.getMyInfo(loginId, loginPw));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader("X-Huuim-LoginId") String loginId,
            @RequestHeader("X-Huuim-LoginPw") String loginPw,
            @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(loginId, loginPw, request.newPassword());
        return ApiResponse.success(HttpStatus.OK, "비밀번호가 변경되었습니다", null);
    }
}
