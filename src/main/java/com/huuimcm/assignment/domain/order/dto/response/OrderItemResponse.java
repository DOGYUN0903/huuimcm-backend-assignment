package com.huuimcm.assignment.domain.order.dto.response;

import com.huuimcm.assignment.domain.order.entity.OrderItem;

public record OrderItemResponse(
        Long productId,
        String productName,
        Integer quantity,
        Long orderPrice
) {
    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getProduct().getId(),
                orderItem.getProduct().getName(),
                orderItem.getQuantity(),
                orderItem.getOrderPrice()
        );
    }
}
