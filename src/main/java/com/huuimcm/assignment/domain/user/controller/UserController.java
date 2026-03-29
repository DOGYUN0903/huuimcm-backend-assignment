package com.huuimcm.assignment.domain.user.controller;

import com.huuimcm.assignment.domain.user.dto.request.PasswordChangeRequest;
import com.huuimcm.assignment.domain.user.dto.request.UserCreateRequest;
import com.huuimcm.assignment.domain.user.dto.response.UserCreateResponse;
import com.huuimcm.assignment.domain.user.dto.response.UserInfoResponse;
import com.huuimcm.assignment.domain.user.service.UserService;
import com.huuimcm.assignment.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "유저", description = "회원가입, 내 정보 조회, 비밀번호 변경")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "새로운 유저를 등록합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패 (빈 값, 길이 초과 등)", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 로그인 ID", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<UserCreateResponse>> signup(
            @Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.success(HttpStatus.CREATED, "회원가입이 완료되었습니다", userService.signup(request));
    }

    @Operation(summary = "내 정보 조회", description = "로그인한 유저의 정보를 조회합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (존재하지 않는 유저 또는 비밀번호 불일치)", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyInfo(
            @Parameter(description = "로그인 ID", required = true) @RequestHeader("X-Huuim-LoginId") String loginId,
            @Parameter(description = "로그인 비밀번호", required = true) @RequestHeader("X-Huuim-LoginPw") String loginPw) {
        return ApiResponse.success(HttpStatus.OK, "내 정보 조회 성공", userService.getMyInfo(loginId, loginPw));
    }

    @Operation(summary = "비밀번호 변경", description = "로그인한 유저의 비밀번호를 변경합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패 (빈 값)", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (존재하지 않는 유저 또는 비밀번호 불일치)", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Parameter(description = "로그인 ID", required = true) @RequestHeader("X-Huuim-LoginId") String loginId,
            @Parameter(description = "로그인 비밀번호", required = true) @RequestHeader("X-Huuim-LoginPw") String loginPw,
            @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(loginId, loginPw, request.newPassword());
        return ApiResponse.success(HttpStatus.OK, "비밀번호가 변경되었습니다", null);
    }
}
