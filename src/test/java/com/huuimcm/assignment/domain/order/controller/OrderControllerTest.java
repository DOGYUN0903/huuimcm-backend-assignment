package com.huuimcm.assignment.domain.order.controller;

import com.huuimcm.assignment.domain.order.dto.request.OrderCreateRequest;
import com.huuimcm.assignment.domain.order.dto.request.OrderItemRequest;
import com.huuimcm.assignment.domain.product.dto.request.ProductCreateRequest;
import com.huuimcm.assignment.domain.product.dto.response.ProductCreateResponse;
import com.huuimcm.assignment.domain.product.service.ProductService;
import com.huuimcm.assignment.domain.user.dto.request.UserCreateRequest;
import com.huuimcm.assignment.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @BeforeEach
    void setUp() {
        userService.signup(new UserCreateRequest("buyer", "password123", "구매자"));
        userService.signup(new UserCreateRequest("seller", "password123", "판매자"));
    }

    private Long createProduct() {
        ProductCreateResponse response = productService.create("seller", "password123",
                new ProductCreateRequest("나이키 에어맥스", "운동화 설명", 159000L, 100, "나이키"));
        return response.id();
    }

    @Nested
    @DisplayName("POST /api/v1/orders - 주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            Long productId = createProduct();
            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(new OrderItemRequest(productId, 2)));

            mockMvc.perform(post("/api/v1/orders")
                            .header("X-Huuim-LoginId", "buyer")
                            .header("X-Huuim-LoginPw", "password123")
                            .header("Idempotency-Key", UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("주문이 완료되었습니다"))
                    .andExpect(jsonPath("$.data.totalPrice").value(318000))
                    .andExpect(jsonPath("$.data.orderItems.length()").value(1));
        }

        @Test
        @DisplayName("성공 - 멱등성 키 중복 시 기존 주문 반환")
        void success_idempotency() throws Exception {
            Long productId = createProduct();
            String idempotencyKey = UUID.randomUUID().toString();
            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(new OrderItemRequest(productId, 1)));

            mockMvc.perform(post("/api/v1/orders")
                    .header("X-Huuim-LoginId", "buyer")
                    .header("X-Huuim-LoginPw", "password123")
                    .header("Idempotency-Key", idempotencyKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            mockMvc.perform(post("/api/v1/orders")
                            .header("X-Huuim-LoginId", "buyer")
                            .header("X-Huuim-LoginPw", "password123")
                            .header("Idempotency-Key", idempotencyKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.totalPrice").value(159000));
        }

        @Test
        @DisplayName("성공 - 다른 유저의 같은 멱등성 키는 별도 주문으로 처리")
        void success_sameIdempotencyKeyAcrossDifferentUsers() throws Exception {
            userService.signup(new UserCreateRequest("buyer2", "password123", "구매자2"));

            Long productId = createProduct();
            String idempotencyKey = UUID.randomUUID().toString();
            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(new OrderItemRequest(productId, 1)));

            String firstResponse = mockMvc.perform(post("/api/v1/orders")
                            .header("X-Huuim-LoginId", "buyer")
                            .header("X-Huuim-LoginPw", "password123")
                            .header("Idempotency-Key", idempotencyKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String secondResponse = mockMvc.perform(post("/api/v1/orders")
                            .header("X-Huuim-LoginId", "buyer2")
                            .header("X-Huuim-LoginPw", "password123")
                            .header("Idempotency-Key", idempotencyKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Long firstOrderId = objectMapper.readTree(firstResponse).get("data").get("id").asLong();
            Long secondOrderId = objectMapper.readTree(secondResponse).get("data").get("id").asLong();

            assertThat(secondOrderId).isNotEqualTo(firstOrderId);
        }

        @Test
        @DisplayName("실패 - 인증 실패")
        void fail_unauthorized() throws Exception {
            Long productId = createProduct();
            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(new OrderItemRequest(productId, 1)));

            mockMvc.perform(post("/api/v1/orders")
                            .header("X-Huuim-LoginId", "buyer")
                            .header("X-Huuim-LoginPw", "wrongPassword")
                            .header("Idempotency-Key", UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 주문 상품 누락")
        void fail_emptyItems() throws Exception {
            OrderCreateRequest request = new OrderCreateRequest(List.of());

            mockMvc.perform(post("/api/v1/orders")
                            .header("X-Huuim-LoginId", "buyer")
                            .header("X-Huuim-LoginPw", "password123")
                            .header("Idempotency-Key", UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders - 주문 목록 조회")
    class GetOrders {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            Long productId = createProduct();
            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(new OrderItemRequest(productId, 1)));

            mockMvc.perform(post("/api/v1/orders")
                    .header("X-Huuim-LoginId", "buyer")
                    .header("X-Huuim-LoginPw", "password123")
                    .header("Idempotency-Key", UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            mockMvc.perform(get("/api/v1/orders")
                            .header("X-Huuim-LoginId", "buyer")
                            .header("X-Huuim-LoginPw", "password123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.data.length()").value(1))
                    .andExpect(jsonPath("$.data.data[0].totalPrice").value(159000));
        }

        @Test
        @DisplayName("성공 - 주문이 없는 경우")
        void success_empty() throws Exception {
            mockMvc.perform(get("/api/v1/orders")
                            .header("X-Huuim-LoginId", "buyer")
                            .header("X-Huuim-LoginPw", "password123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{orderId} - 주문 상세 조회")
    class GetOrder {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            Long productId = createProduct();
            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(new OrderItemRequest(productId, 2)));

            String response = mockMvc.perform(post("/api/v1/orders")
                            .header("X-Huuim-LoginId", "buyer")
                            .header("X-Huuim-LoginPw", "password123")
                            .header("Idempotency-Key", UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn().getResponse().getContentAsString();

            Long orderId = objectMapper.readTree(response).get("data").get("id").asLong();

            mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                            .header("X-Huuim-LoginId", "buyer")
                            .header("X-Huuim-LoginPw", "password123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalPrice").value(318000))
                    .andExpect(jsonPath("$.data.orderItems.length()").value(1));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문")
        void fail_notFound() throws Exception {
            mockMvc.perform(get("/api/v1/orders/999")
                            .header("X-Huuim-LoginId", "buyer")
                            .header("X-Huuim-LoginPw", "password123"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 타인의 주문 조회")
        void fail_accessDenied() throws Exception {
            Long productId = createProduct();
            OrderCreateRequest request = new OrderCreateRequest(
                    List.of(new OrderItemRequest(productId, 1)));

            String response = mockMvc.perform(post("/api/v1/orders")
                            .header("X-Huuim-LoginId", "buyer")
                            .header("X-Huuim-LoginPw", "password123")
                            .header("Idempotency-Key", UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn().getResponse().getContentAsString();

            Long orderId = objectMapper.readTree(response).get("data").get("id").asLong();

            mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                            .header("X-Huuim-LoginId", "seller")
                            .header("X-Huuim-LoginPw", "password123"))
                    .andExpect(status().isForbidden());
        }
    }
}
