package com.huuimcm.assignment.domain.product.dto.response;

import com.huuimcm.assignment.domain.product.entity.Product;

import java.time.LocalDateTime;

public record ProductListResponse(
        Long id,
        String name,
        Long price,
        String brand,
        Integer likeCount,
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
