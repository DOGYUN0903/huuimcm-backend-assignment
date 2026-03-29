package com.huuimcm.assignment.domain.order.service;

import com.huuimcm.assignment.domain.order.dto.request.OrderCreateRequest;
import com.huuimcm.assignment.domain.order.dto.request.OrderItemRequest;
import com.huuimcm.assignment.domain.order.dto.response.OrderCreateResponse;
import com.huuimcm.assignment.domain.order.dto.response.OrderListResponse;
import com.huuimcm.assignment.domain.order.entity.Order;
import com.huuimcm.assignment.domain.order.exception.OrderException;
import com.huuimcm.assignment.domain.order.repository.OrderRepository;
import com.huuimcm.assignment.domain.product.entity.Product;
import com.huuimcm.assignment.domain.product.service.ProductService;
import com.huuimcm.assignment.domain.user.entity.User;
import com.huuimcm.assignment.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            User user = User.create("user1", "encodedPw", "테스트유저");
            ReflectionTestUtils.setField(user, "id", 1L);
            Product product = Product.create(user, "나이키 에어맥스", "운동화 설명", 159000L, 100, "나이키");
            OrderCreateRequest request = new OrderCreateRequest(List.of(new OrderItemRequest(1L, 2)));

            given(userService.authenticate("user1", "password")).willReturn(user);
            given(orderRepository.findByUserIdAndIdempotencyKey(1L, "key-1")).willReturn(Optional.empty());
            given(productService.getProductWithLock(1L)).willReturn(product);
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            OrderCreateResponse response = orderService.createOrder("user1", "password", "key-1", request);

            // then
            assertThat(response.totalPrice()).isEqualTo(318000L);
            assertThat(response.orderItems()).hasSize(1);
            assertThat(product.getStock()).isEqualTo(98);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("성공 - 멱등성 키 중복 시 기존 주문 반환")
        void success_idempotency() {
            // given
            User user = User.create("user1", "encodedPw", "테스트유저");
            ReflectionTestUtils.setField(user, "id", 1L);
            Order existingOrder = Order.create(user, "key-1");
            OrderCreateRequest request = new OrderCreateRequest(List.of(new OrderItemRequest(1L, 1)));

            given(userService.authenticate("user1", "password")).willReturn(user);
            given(orderRepository.findByUserIdAndIdempotencyKey(1L, "key-1")).willReturn(Optional.of(existingOrder));

            // when
            OrderCreateResponse response = orderService.createOrder("user1", "password", "key-1", request);

            // then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("성공 - 다른 유저의 같은 멱등성 키는 별도 주문으로 처리")
        void success_sameIdempotencyKeyAcrossDifferentUsers() {
            // given
            User seller = User.create("seller", "encodedPw", "판매자");
            User user = User.create("user2", "encodedPw", "다른유저");
            ReflectionTestUtils.setField(user, "id", 2L);
            Product product = Product.create(seller, "상품", "설명", 10000L, 10, "브랜드");
            OrderCreateRequest request = new OrderCreateRequest(List.of(new OrderItemRequest(1L, 1)));

            given(userService.authenticate("user2", "password")).willReturn(user);
            given(orderRepository.findByUserIdAndIdempotencyKey(2L, "shared-key")).willReturn(Optional.empty());
            given(productService.getProductWithLock(1L)).willReturn(product);
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            OrderCreateResponse response = orderService.createOrder("user2", "password", "shared-key", request);

            // then
            assertThat(response).isNotNull();
            verify(orderRepository).findByUserIdAndIdempotencyKey(2L, "shared-key");
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("성공 - 여러 상품 동시 주문")
        void success_multipleItems() {
            // given
            User user = User.create("user1", "encodedPw", "테스트유저");
            ReflectionTestUtils.setField(user, "id", 1L);
            Product product1 = Product.create(user, "상품1", "설명1", 10000L, 50, "브랜드");
            Product product2 = Product.create(user, "상품2", "설명2", 20000L, 30, "브랜드");
            OrderCreateRequest request = new OrderCreateRequest(List.of(
                    new OrderItemRequest(1L, 2),
                    new OrderItemRequest(2L, 3)
            ));

            given(userService.authenticate("user1", "password")).willReturn(user);
            given(orderRepository.findByUserIdAndIdempotencyKey(1L, "key-2")).willReturn(Optional.empty());
            given(productService.getProductWithLock(1L)).willReturn(product1);
            given(productService.getProductWithLock(2L)).willReturn(product2);
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            OrderCreateResponse response = orderService.createOrder("user1", "password", "key-2", request);

            // then
            assertThat(response.totalPrice()).isEqualTo(80000L);
            assertThat(response.orderItems()).hasSize(2);
            assertThat(product1.getStock()).isEqualTo(48);
            assertThat(product2.getStock()).isEqualTo(27);
        }
    }

    @Nested
    @DisplayName("주문 목록 조회")
    class GetOrders {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            User user = User.create("user1", "encodedPw", "테스트유저");
            ReflectionTestUtils.setField(user, "id", 1L);
            Order order = Order.create(user, "key-1");
            Page<Order> page = new PageImpl<>(List.of(order), PageRequest.of(0, 20), 1);

            given(userService.authenticate("user1", "password")).willReturn(user);
            given(orderRepository.findOrdersByUserId(1L, PageRequest.of(0, 20))).willReturn(page);

            // when
            Page<OrderListResponse> response = orderService.getOrders("user1", "password", 0, 20);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("주문 상세 조회")
    class GetOrder {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            User user = User.create("user1", "encodedPw", "테스트유저");
            ReflectionTestUtils.setField(user, "id", 1L);
            Order order = Order.create(user, "key-1");

            given(userService.authenticate("user1", "password")).willReturn(user);
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            // when
            OrderListResponse response = orderService.getOrder("user1", "password", 1L);

            // then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문")
        void fail_orderNotFound() {
            // given
            User user = User.create("user1", "encodedPw", "테스트유저");

            given(userService.authenticate("user1", "password")).willReturn(user);
            given(orderRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.getOrder("user1", "password", 999L))
                    .isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("실패 - 타 유저의 주문 접근")
        void fail_accessDenied() {
            // given
            User user = User.create("user1", "encodedPw", "테스트유저");
            ReflectionTestUtils.setField(user, "id", 1L);

            User otherUser = User.create("other", "encodedPw", "다른유저");
            ReflectionTestUtils.setField(otherUser, "id", 2L);
            Order otherOrder = Order.create(otherUser, "key-1");

            given(userService.authenticate("user1", "password")).willReturn(user);
            given(orderRepository.findById(1L)).willReturn(Optional.of(otherOrder));

            // when & then
            assertThatThrownBy(() -> orderService.getOrder("user1", "password", 1L))
                    .isInstanceOf(OrderException.class);
        }
    }
}
