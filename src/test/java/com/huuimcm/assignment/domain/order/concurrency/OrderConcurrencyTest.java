package com.huuimcm.assignment.domain.order.concurrency;

import com.huuimcm.assignment.domain.order.dto.request.OrderCreateRequest;
import com.huuimcm.assignment.domain.order.dto.request.OrderItemRequest;
import com.huuimcm.assignment.domain.order.repository.OrderRepository;
import com.huuimcm.assignment.domain.order.service.OrderService;
import com.huuimcm.assignment.domain.product.dto.request.ProductCreateRequest;
import com.huuimcm.assignment.domain.product.dto.response.ProductCreateResponse;
import com.huuimcm.assignment.domain.product.entity.Product;
import com.huuimcm.assignment.domain.product.repository.ProductRepository;
import com.huuimcm.assignment.domain.product.service.ProductService;
import com.huuimcm.assignment.domain.user.dto.request.UserCreateRequest;
import com.huuimcm.assignment.domain.user.repository.UserRepository;
import com.huuimcm.assignment.domain.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OrderConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userService.signup(new UserCreateRequest("buyer", "password123", "구매자"));
        userService.signup(new UserCreateRequest("seller", "password123", "판매자"));
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("동시에 10건 주문 시 재고가 정확히 차감된다")
    void concurrentOrderDecreaseStock() throws InterruptedException {
        ProductCreateResponse product = productService.create("seller", "password123",
                new ProductCreateRequest("동시성 테스트 상품", "설명", 10000L, 100, "브랜드"));
        Long productId = product.id();

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    OrderCreateRequest request = new OrderCreateRequest(
                            List.of(new OrderItemRequest(productId, 1)));
                    orderService.createOrder("buyer", "password123", UUID.randomUUID().toString(), request);
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Product result = productRepository.findById(productId).orElseThrow();
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(result.getStock()).isEqualTo(90);
    }

    @Test
    @DisplayName("재고보다 많은 동시 주문 시 일부만 성공한다")
    void concurrentOrderExceedStock() throws InterruptedException {
        ProductCreateResponse product = productService.create("seller", "password123",
                new ProductCreateRequest("재고 부족 테스트 상품", "설명", 10000L, 5, "브랜드"));
        Long productId = product.id();

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    OrderCreateRequest request = new OrderCreateRequest(
                            List.of(new OrderItemRequest(productId, 1)));
                    orderService.createOrder("buyer", "password123", UUID.randomUUID().toString(), request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Product result = productRepository.findById(productId).orElseThrow();
        assertThat(successCount.get()).isEqualTo(5);
        assertThat(failCount.get()).isEqualTo(5);
        assertThat(result.getStock()).isEqualTo(0);
    }
}
