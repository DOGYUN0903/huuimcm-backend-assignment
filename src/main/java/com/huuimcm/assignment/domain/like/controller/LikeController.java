package com.huuimcm.assignment.domain.like.controller;

import com.huuimcm.assignment.domain.like.service.LikeService;
import com.huuimcm.assignment.domain.product.dto.response.ProductListResponse;
import com.huuimcm.assignment.global.response.ApiResponse;
import com.huuimcm.assignment.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/api/v1/products/{productId}/likes")
    public ResponseEntity<ApiResponse<Void>> toggleLike(
            @RequestHeader("X-Huuim-LoginId") String loginId,
            @RequestHeader("X-Huuim-LoginPw") String loginPw,
            @PathVariable Long productId) {
        likeService.toggleLike(loginId, loginPw, productId);
        return ApiResponse.success(HttpStatus.OK, "좋아요가 처리되었습니다", null);
    }

    @GetMapping("/api/v1/products/liked")
    public ResponseEntity<ApiResponse<PageResponse<ProductListResponse>>> getLikedProducts(
            @RequestHeader("X-Huuim-LoginId") String loginId,
            @RequestHeader("X-Huuim-LoginPw") String loginPw,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(HttpStatus.OK, "좋아요한 상품 목록을 조회하였습니다", likeService.getLikedProducts(loginId, loginPw, page, size));
    }
}
