package com.huuimcm.assignment.domain.product.entity;

import com.huuimcm.assignment.domain.product.exception.ProductErrorCode;
import com.huuimcm.assignment.domain.product.exception.ProductException;
import com.huuimcm.assignment.domain.user.entity.User;
import com.huuimcm.assignment.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_created_at", columnList = "created_at DESC"),
        @Index(name = "idx_product_price", columnList = "price ASC"),
        @Index(name = "idx_product_like_count", columnList = "like_count DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(nullable = false)
    private Integer likeCount = 0;

    private Product(User seller, String name, String description, Long price, Integer stock, String brand) {
        this.seller = seller;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.brand = brand;
    }

    public static Product create(User seller, String name, String description, Long price, Integer stock, String brand) {
        return new Product(seller, name, description, price, stock, brand);
    }

    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new ProductException(ProductErrorCode.INSUFFICIENT_STOCK);
        }
        this.stock -= quantity;
    }
}
