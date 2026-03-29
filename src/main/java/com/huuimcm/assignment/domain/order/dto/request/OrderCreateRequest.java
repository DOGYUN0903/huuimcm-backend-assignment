package com.huuimcm.assignment.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "주문 생성 요청")
public record OrderCreateRequest(

        @Schema(description = "주문 상품 목록")
        @Valid
        @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다")
        List<OrderItemRequest> orderItems
) {
}
