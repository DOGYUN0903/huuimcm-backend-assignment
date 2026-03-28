package com.huuimcm.assignment.domain.product.controller;

import com.huuimcm.assignment.domain.product.dto.request.ProductCreateRequest;
import com.huuimcm.assignment.domain.product.dto.response.ProductCreateResponse;
import com.huuimcm.assignment.domain.product.dto.response.ProductDetailResponse;
import com.huuimcm.assignment.domain.product.dto.response.ProductListResponse;
import com.huuimcm.assignment.domain.product.service.ProductService;
import com.huuimcm.assignment.global.response.ApiResponse;
import com.huuimcm.assignment.global.response.PageResponse;
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

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductCreateResponse>> create(
            @RequestHeader("X-Huuim-LoginId") String loginId,
            @RequestHeader("X-Huuim-LoginPw") String loginPw,
            @Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.success(HttpStatus.CREATED, "상품이 등록되었습니다", productService.create(loginId, loginPw, request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductListResponse>>> getProducts(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(HttpStatus.OK, "상품 목록을 조회하였습니다", productService.getProducts(sort, page, size));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(
            @PathVariable Long productId) {
        return ApiResponse.success(HttpStatus.OK, "상품 상세 조회 성공", productService.getProductDetail(productId));
    }
}
