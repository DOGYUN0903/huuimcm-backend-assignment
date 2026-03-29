package com.huuimcm.assignment.domain.like.concurrency;

import com.huuimcm.assignment.domain.like.repository.LikeRepository;
import com.huuimcm.assignment.domain.like.service.LikeService;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LikeConcurrencyTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userService.signup(new UserCreateRequest("seller", "password123", "판매자"));
    }

    @AfterEach
    void tearDown() {
        likeRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("10명이 동시에 좋아요를 누르면 likeCount가 정확히 10이 된다")
    void concurrentLikeFromDifferentUsers() throws InterruptedException {
        ProductCreateResponse product = productService.create("seller", "password123",
                new ProductCreateRequest("좋아요 테스트 상품", "설명", 10000L, 100, "브랜드"));
        Long productId = product.id();

        int threadCount = 10;
        for (int i = 0; i < threadCount; i++) {
            userService.signup(new UserCreateRequest("likeuser" + i, "password123", "유저" + i));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            int userIndex = i;
            executorService.submit(() -> {
                try {
                    likeService.toggleLike("likeuser" + userIndex, "password123", productId);
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
        assertThat(result.getLikeCount()).isEqualTo(10);
    }
}
