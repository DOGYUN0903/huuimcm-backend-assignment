package com.huuimcm.assignment.domain.order.controller;

import com.huuimcm.assignment.domain.order.dto.request.OrderCreateRequest;
import com.huuimcm.assignment.domain.order.dto.response.OrderCreateResponse;
import com.huuimcm.assignment.domain.order.dto.response.OrderListResponse;
import com.huuimcm.assignment.domain.order.service.OrderService;
import com.huuimcm.assignment.global.response.ApiResponse;
import com.huuimcm.assignment.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "주문", description = "주문 생성, 주문 목록 조회, 주문 상세 조회")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 생성", description = "상품을 주문합니다. Idempotency-Key 헤더로 멱등성을 보장합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패 (빈 주문 목록, 재고 부족 등)", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @Parameter(description = "로그인 ID", required = true) @RequestHeader("X-Huuim-LoginId") String loginId,
            @Parameter(description = "로그인 비밀번호", required = true) @RequestHeader("X-Huuim-LoginPw") String loginPw,
            @Parameter(description = "멱등성 키 (중복 주문 방지)", required = true, example = "550e8400-e29b-41d4-a716-446655440000") @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.success(HttpStatus.CREATED, "주문이 완료되었습니다", orderService.createOrder(loginId, loginPw, idempotencyKey, request));
    }

    @Operation(summary = "주문 목록 조회", description = "내 주문 목록을 페이징 조회합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderListResponse>>> getOrders(
            @Parameter(description = "로그인 ID", required = true) @RequestHeader("X-Huuim-LoginId") String loginId,
            @Parameter(description = "로그인 비밀번호", required = true) @RequestHeader("X-Huuim-LoginPw") String loginPw,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(HttpStatus.OK, "주문 목록 조회 성공", new PageResponse<>(orderService.getOrders(loginId, loginPw, page, size)));
    }

    @Operation(summary = "주문 상세 조회", description = "주문 ID로 주문 상세 정보를 조회합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 주문에 접근 권한 없음", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderListResponse>> getOrder(
            @Parameter(description = "로그인 ID", required = true) @RequestHeader("X-Huuim-LoginId") String loginId,
            @Parameter(description = "로그인 비밀번호", required = true) @RequestHeader("X-Huuim-LoginPw") String loginPw,
            @Parameter(description = "주문 ID", example = "1") @PathVariable Long orderId) {
        return ApiResponse.success(HttpStatus.OK, "주문 상세 조회 성공", orderService.getOrder(loginId, loginPw, orderId));
    }
}
