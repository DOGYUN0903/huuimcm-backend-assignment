package com.huuimcm.assignment.domain.product.dto.response;

import com.huuimcm.assignment.domain.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "상품 목록 응답")
public record ProductListResponse(
        @Schema(description = "상품 ID", example = "1")
        Long id,
        @Schema(description = "상품명", example = "나이키 에어맥스 90")
        String name,
        @Schema(description = "가격 (원)", example = "139000")
        Long price,
        @Schema(description = "브랜드", example = "Nike")
        String brand,
        @Schema(description = "좋아요 수", example = "42")
        Integer likeCount,
        @Schema(description = "등록일시", example = "2026-03-29T10:30:00")
        LocalDateTime createdAt
) {
    public static ProductListResponse from(Product product) {
        return new ProductListResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getBrand(),
                product.getLikeCount(),
                product.getCreatedAt()
        );
    }
}
