package com.huuimcm.assignment.domain.like.controller;

import com.huuimcm.assignment.domain.like.service.LikeService;
import com.huuimcm.assignment.domain.product.dto.response.ProductListResponse;
import com.huuimcm.assignment.global.response.ApiResponse;
import com.huuimcm.assignment.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "좋아요", description = "좋아요 토글, 좋아요한 상품 목록 조회")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "좋아요 토글", description = "상품에 좋아요를 등록하거나 취소합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좋아요 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/{productId}/likes")
    public ResponseEntity<ApiResponse<Void>> toggleLike(
            @Parameter(description = "로그인 ID", required = true) @RequestHeader("X-Huuim-LoginId") String loginId,
            @Parameter(description = "로그인 비밀번호", required = true) @RequestHeader("X-Huuim-LoginPw") String loginPw,
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId) {
        likeService.toggleLike(loginId, loginPw, productId);
        return ApiResponse.success(HttpStatus.OK, "좋아요가 처리되었습니다", null);
    }

    @Operation(summary = "좋아요한 상품 목록 조회", description = "내가 좋아요한 상품 목록을 페이징 조회합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/liked")
    public ResponseEntity<ApiResponse<PageResponse<ProductListResponse>>> getLikedProducts(
            @Parameter(description = "로그인 ID", required = true) @RequestHeader("X-Huuim-LoginId") String loginId,
            @Parameter(description = "로그인 비밀번호", required = true) @RequestHeader("X-Huuim-LoginPw") String loginPw,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(HttpStatus.OK, "좋아요한 상품 목록을 조회하였습니다", likeService.getLikedProducts(loginId, loginPw, page, size));
    }
}
