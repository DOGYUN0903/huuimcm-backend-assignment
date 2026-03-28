package com.huuimcm.assignment.domain.order.service;

import com.huuimcm.assignment.domain.order.dto.request.OrderCreateRequest;
import com.huuimcm.assignment.domain.order.dto.response.OrderCreateResponse;
import com.huuimcm.assignment.domain.order.dto.response.OrderListResponse;
import com.huuimcm.assignment.domain.order.entity.Order;
import com.huuimcm.assignment.domain.order.exception.OrderErrorCode;
import com.huuimcm.assignment.domain.order.exception.OrderException;
import com.huuimcm.assignment.domain.order.repository.OrderRepository;
import com.huuimcm.assignment.domain.product.entity.Product;
import com.huuimcm.assignment.domain.product.service.ProductService;
import com.huuimcm.assignment.domain.user.entity.User;
import com.huuimcm.assignment.domain.user.service.UserService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;

    @Transactional
    public OrderCreateResponse createOrder(String loginId, String loginPw, String idempotencyKey, OrderCreateRequest request) {
        User user = userService.authenticate(loginId, loginPw);

        Optional<Order> existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
        if (existingOrder.isPresent()) {
            return OrderCreateResponse.from(existingOrder.get());
        }

        Order order = Order.create(user, idempotencyKey);

        request.orderItems().forEach(item -> {
            Product product = productService.getProductWithLock(item.productId());
            product.decreaseStock(item.quantity());
            order.addOrderItem(product, item.quantity());
        });

        Order savedOrder = orderRepository.save(order);
        return OrderCreateResponse.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderListResponse> getOrders(String loginId, String loginPw, int page, int size) {
        User user = userService.authenticate(loginId, loginPw);
        Page<Order> orders = orderRepository.findOrdersByUserId(user.getId(), PageRequest.of(page, size));
        return orders.map(OrderListResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderListResponse getOrder(String loginId, String loginPw, Long orderId) {
        User user = userService.authenticate(loginId, loginPw);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new OrderException(OrderErrorCode.ORDER_ACCESS_DENIED);
        }

        return OrderListResponse.from(order);
    }
}
