package com.huuimcm.assignment.domain.order.controller;

import com.huuimcm.assignment.domain.order.dto.request.OrderCreateRequest;
import com.huuimcm.assignment.domain.order.dto.response.OrderCreateResponse;
import com.huuimcm.assignment.domain.order.dto.response.OrderListResponse;
import com.huuimcm.assignment.domain.order.service.OrderService;
import com.huuimcm.assignment.global.response.ApiResponse;
import com.huuimcm.assignment.global.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @RequestHeader("X-Huuim-LoginId") String loginId,
            @RequestHeader("X-Huuim-LoginPw") String loginPw,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.success(HttpStatus.CREATED, "주문이 완료되었습니다", orderService.createOrder(loginId, loginPw, idempotencyKey, request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderListResponse>>> getOrders(
            @RequestHeader("X-Huuim-LoginId") String loginId,
            @RequestHeader("X-Huuim-LoginPw") String loginPw,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(HttpStatus.OK, "주문 목록 조회 성공", new PageResponse<>(orderService.getOrders(loginId, loginPw, page, size)));
    }
}
