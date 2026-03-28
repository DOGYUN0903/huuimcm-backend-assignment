package com.huuimcm.assignment.domain.product.dto.response;

import com.huuimcm.assignment.domain.product.entity.Product;

public record ProductCreateResponse(
        Long id,
        String name,
        Long price,
        Integer stock,
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
