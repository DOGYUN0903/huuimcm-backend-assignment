package com.huuimcm.assignment.domain.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "상품 등록 요청")
public record ProductCreateRequest(

        @Schema(description = "상품명", example = "나이키 에어맥스 90")
        @NotBlank(message = "상품명은 필수입니다")
        @Size(max = 200, message = "상품명은 200자 이하여야 합니다")
        String name,

        @Schema(description = "상품 설명", example = "클래식한 디자인의 러닝화")
        @NotBlank(message = "상품 설명은 필수입니다")
        @Size(max = 2000, message = "상품 설명은 2000자 이하여야 합니다")
        String description,

        @Schema(description = "가격 (원)", example = "139000")
        @NotNull(message = "가격은 필수입니다")
        @Min(value = 0, message = "가격은 0 이상이어야 합니다")
        Long price,

        @Schema(description = "재고 수량", example = "100")
        @NotNull(message = "재고는 필수입니다")
        @Min(value = 0, message = "재고는 0 이상이어야 합니다")
        Integer stock,

        @Schema(description = "브랜드", example = "Nike")
        @NotBlank(message = "브랜드는 필수입니다")
        @Size(max = 100, message = "브랜드는 100자 이하여야 합니다")
        String brand
) {}
