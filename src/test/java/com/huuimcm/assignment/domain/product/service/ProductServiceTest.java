package com.huuimcm.assignment.domain.product.service;

import com.huuimcm.assignment.domain.product.dto.request.ProductCreateRequest;
import com.huuimcm.assignment.domain.product.dto.response.ProductCreateResponse;
import com.huuimcm.assignment.domain.product.dto.response.ProductDetailResponse;
import com.huuimcm.assignment.domain.product.entity.Product;
import com.huuimcm.assignment.domain.product.exception.ProductException;
import com.huuimcm.assignment.domain.product.repository.ProductRepository;
import com.huuimcm.assignment.domain.user.entity.User;
import com.huuimcm.assignment.domain.user.service.UserService;
import com.huuimcm.assignment.global.response.PageResponse;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserService userService;

    private User createSeller() {
        return User.create("seller", "encodedPw", "판매자");
    }

    private Product createProduct(User seller) {
        return Product.create(seller, "나이키 에어맥스", "운동화 설명", 159000L, 100, "나이키");
    }

    @Nested
    @DisplayName("상품 등록")
    class Create {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            User seller = createSeller();
            ProductCreateRequest request = new ProductCreateRequest("나이키 에어맥스", "운동화 설명", 159000L, 100, "나이키");
            Product product = createProduct(seller);

            given(userService.authenticate("seller", "password")).willReturn(seller);
            given(productRepository.save(any(Product.class))).willReturn(product);

            // when
            ProductCreateResponse response = productService.create("seller", "password", request);

            // then
            assertThat(response.name()).isEqualTo("나이키 에어맥스");
            verify(productRepository).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("상품 목록 조회")
    class GetProducts {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            User seller = createSeller();
            Product product = createProduct(seller);
            Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1);

            given(productRepository.findProducts(any(PageRequest.class), eq("latest"))).willReturn(page);

            // when
            PageResponse<?> response = productService.getProducts("latest", 0, 20);

            // then
            assertThat(response.getData()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("상품 상세 조회")
    class GetProductDetail {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            User seller = createSeller();
            Product product = createProduct(seller);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            // when
            ProductDetailResponse response = productService.getProductDetail(1L);

            // then
            assertThat(response.name()).isEqualTo("나이키 에어맥스");
            assertThat(response.brand()).isEqualTo("나이키");
            assertThat(response.price()).isEqualTo(159000L);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상품")
        void fail_productNotFound() {
            // given
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.getProductDetail(999L))
                    .isInstanceOf(ProductException.class);
        }
    }

    @Nested
    @DisplayName("상품 조회 (내부용)")
    class GetProduct {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            User seller = createSeller();
            Product product = createProduct(seller);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            // when
            Product result = productService.getProduct(1L);

            // then
            assertThat(result.getName()).isEqualTo("나이키 에어맥스");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상품")
        void fail_productNotFound() {
            // given
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.getProduct(999L))
                    .isInstanceOf(ProductException.class);
        }
    }

    @Nested
    @DisplayName("비관적 락 상품 조회")
    class GetProductWithLock {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            User seller = createSeller();
            Product product = createProduct(seller);

            given(productRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(product));

            // when
            Product result = productService.getProductWithLock(1L);

            // then
            assertThat(result.getName()).isEqualTo("나이키 에어맥스");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상품")
        void fail_productNotFound() {
            // given
            given(productRepository.findByIdWithPessimisticLock(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.getProductWithLock(999L))
                    .isInstanceOf(ProductException.class);
        }
    }

    @Nested
    @DisplayName("좋아요 수 변경")
    class LikeCount {

        @Test
        @DisplayName("좋아요 수 증가")
        void increaseLikeCount() {
            // when
            productService.increaseLikeCount(1L);

            // then
            verify(productRepository).increaseLikeCount(1L);
        }

        @Test
        @DisplayName("좋아요 수 감소")
        void decreaseLikeCount() {
            // when
            productService.decreaseLikeCount(1L);

            // then
            verify(productRepository).decreaseLikeCount(1L);
        }
    }
}
