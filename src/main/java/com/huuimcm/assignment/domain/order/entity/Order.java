package com.huuimcm.assignment.domain.order.entity;

import com.huuimcm.assignment.domain.product.entity.Product;
import com.huuimcm.assignment.domain.user.entity.User;
import com.huuimcm.assignment.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private Long totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    private Order(User user, String idempotencyKey) {
        this.user = user;
        this.idempotencyKey = idempotencyKey;
        this.totalPrice = 0L;
    }

    public static Order create(User user, String idempotencyKey) {
        return new Order(user, idempotencyKey);
    }

    public void addOrderItem(Product product, int quantity) {
        OrderItem orderItem = OrderItem.create(this, product, quantity);
        this.orderItems.add(orderItem);
        this.totalPrice += orderItem.getOrderPrice();
    }
}
