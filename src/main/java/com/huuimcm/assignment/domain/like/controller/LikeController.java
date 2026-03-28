package com.huuimcm.assignment.domain.like.controller;

import com.huuimcm.assignment.domain.like.service.LikeService;
import com.huuimcm.assignment.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products/{productId}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> toggleLike(
            @RequestHeader("X-Huuim-LoginId") String loginId,
            @RequestHeader("X-Huuim-LoginPw") String loginPw,
            @PathVariable Long productId) {
        likeService.toggleLike(loginId, loginPw, productId);
        return ApiResponse.success(HttpStatus.OK, "좋아요가 처리되었습니다", null);
    }
}
