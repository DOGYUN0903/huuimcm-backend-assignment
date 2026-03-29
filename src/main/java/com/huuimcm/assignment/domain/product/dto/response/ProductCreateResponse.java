package com.huuimcm.assignment.domain.product.dto.response;

import com.huuimcm.assignment.domain.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 등록 응답")
public record ProductCreateResponse(
        @Schema(description = "상품 ID", example = "1")
        Long id,
        @Schema(description = "상품명", example = "나이키 에어맥스 90")
        String name,
        @Schema(description = "가격 (원)", example = "139000")
        Long price,
        @Schema(description = "재고 수량", example = "100")
        Integer stock,
        @Schema(description = "브랜드", example = "Nike")
        String brand
) {
    public static ProductCreateResponse from(Product product) {
        return new ProductCreateResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getBrand()
        );
    }
}
