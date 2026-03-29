package com.huuimcm.assignment.domain.product.dto.response;

import com.huuimcm.assignment.domain.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "상품 상세 응답")
public record ProductDetailResponse(
        @Schema(description = "상품 ID", example = "1")
        Long id,
        @Schema(description = "상품명", example = "나이키 에어맥스 90")
        String name,
        @Schema(description = "상품 설명", example = "클래식한 디자인의 러닝화")
        String description,
        @Schema(description = "가격 (원)", example = "139000")
        Long price,
        @Schema(description = "재고 수량", example = "100")
        Integer stock,
        @Schema(description = "브랜드", example = "Nike")
        String brand,
        @Schema(description = "좋아요 수", example = "42")
        Integer likeCount,
        @Schema(description = "판매자 이름", example = "홍길동")
        String sellerName,
        @Schema(description = "등록일시", example = "2026-03-29T10:30:00")
        LocalDateTime createdAt
) {
    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getBrand(),
                product.getLikeCount(),
                product.getSeller().getName(),
                product.getCreatedAt()
        );
    }
}
