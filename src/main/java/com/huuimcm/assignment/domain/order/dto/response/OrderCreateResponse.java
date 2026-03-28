package com.huuimcm.assignment.domain.order.dto.response;

import com.huuimcm.assignment.domain.order.entity.Order;

import java.time.LocalDateTime;
import java.util.List;

public record OrderCreateResponse(
        Long id,
        Long totalPrice,
        List<OrderItemResponse> orderItems,
        LocalDateTime createdAt
) {
    public static OrderCreateResponse from(Order order) {
        return new OrderCreateResponse(
                order.getId(),
                order.getTotalPrice(),
                order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                order.getCreatedAt()
        );
    }
}
