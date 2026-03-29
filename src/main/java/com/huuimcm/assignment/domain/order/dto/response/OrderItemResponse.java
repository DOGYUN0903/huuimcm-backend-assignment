package com.huuimcm.assignment.domain.order.dto.response;

import com.huuimcm.assignment.domain.order.entity.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 상품 항목 응답")
public record OrderItemResponse(
        @Schema(description = "상품 ID", example = "1")
        Long productId,
        @Schema(description = "상품명", example = "나이키 에어맥스 90")
        String productName,
        @Schema(description = "주문 수량", example = "2")
        Integer quantity,
        @Schema(description = "주문 금액 (원)", example = "278000")
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
