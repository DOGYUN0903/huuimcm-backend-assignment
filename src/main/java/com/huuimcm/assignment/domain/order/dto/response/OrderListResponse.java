package com.huuimcm.assignment.domain.order.dto.response;

import com.huuimcm.assignment.domain.order.entity.Order;

import java.time.LocalDateTime;
import java.util.List;

public record OrderListResponse(
        Long id,
        Long totalPrice,
        List<OrderItemResponse> orderItems,
        LocalDateTime createdAt
) {
    public static OrderListResponse from(Order order) {
        return new OrderListResponse(
                order.getId(),
                order.getTotalPrice(),
                order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                order.getCreatedAt()
        );
    }
}
