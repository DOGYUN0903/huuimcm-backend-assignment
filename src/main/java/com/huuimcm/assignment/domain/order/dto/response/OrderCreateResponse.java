package com.huuimcm.assignment.domain.order.dto.response;

import com.huuimcm.assignment.domain.order.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주문 생성 응답")
public record OrderCreateResponse(
        @Schema(description = "주문 ID", example = "1")
        Long id,
        @Schema(description = "총 주문 금액 (원)", example = "278000")
        Long totalPrice,
        @Schema(description = "주문 상품 목록")
        List<OrderItemResponse> orderItems,
        @Schema(description = "주문일시", example = "2026-03-29T10:30:00")
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
