package com.huuimcm.assignment.domain.product.controller;

import com.huuimcm.assignment.domain.product.dto.request.ProductCreateRequest;
import com.huuimcm.assignment.domain.product.dto.response.ProductCreateResponse;
import com.huuimcm.assignment.domain.product.dto.response.ProductDetailResponse;
import com.huuimcm.assignment.domain.product.dto.response.ProductListResponse;
import com.huuimcm.assignment.domain.product.service.ProductService;
import com.huuimcm.assignment.global.response.ApiResponse;
import com.huuimcm.assignment.global.response.PageResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "상품", description = "상품 등록, 목록 조회, 상세 조회")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "상품 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패 (빈 값, 음수 가격/재고 등)", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProductCreateResponse>> create(
            @Parameter(description = "로그인 ID", required = true) @RequestHeader("X-Huuim-LoginId") String loginId,
            @Parameter(description = "로그인 비밀번호", required = true) @RequestHeader("X-Huuim-LoginPw") String loginPw,
            @Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.success(HttpStatus.CREATED, "상품이 등록되었습니다", productService.create(loginId, loginPw, request));
    }

    @Operation(summary = "상품 목록 조회", description = "정렬 기준에 따라 상품 목록을 페이징 조회합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductListResponse>>> getProducts(
            @Parameter(description = "정렬 기준 (latest, price_asc, price_desc, popular)", example = "latest") @RequestParam(defaultValue = "latest") String sort,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(HttpStatus.OK, "상품 목록을 조회하였습니다", productService.getProducts(sort, page, size));
    }

    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상세 정보를 조회합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId) {
        return ApiResponse.success(HttpStatus.OK, "상품 상세 조회 성공", productService.getProductDetail(productId));
    }
}
