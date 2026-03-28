package com.huuimcm.assignment.domain.product.dto.response;

import com.huuimcm.assignment.domain.product.entity.Product;

import java.time.LocalDateTime;

public record ProductDetailResponse(
        Long id,
        String name,
        String description,
        Long price,
        Integer stock,
        String brand,
        Integer likeCount,
        String sellerName,
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
