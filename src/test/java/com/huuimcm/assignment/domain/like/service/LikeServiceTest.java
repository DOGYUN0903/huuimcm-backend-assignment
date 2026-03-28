package com.huuimcm.assignment.domain.like.service;

import com.huuimcm.assignment.domain.like.entity.Like;
import com.huuimcm.assignment.domain.like.repository.LikeRepository;
import com.huuimcm.assignment.domain.product.dto.response.ProductListResponse;
import com.huuimcm.assignment.domain.product.entity.Product;
import com.huuimcm.assignment.domain.product.service.ProductService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    private User createUser() {
        return User.create("user1", "encodedPw", "테스트유저");
    }

    private Product createProduct() {
        return Product.create(createUser(), "나이키 에어맥스", "운동화 설명", 159000L, 100, "나이키");
    }

    @Nested
    @DisplayName("좋아요 토글")
    class ToggleLike {

        @Test
        @DisplayName("좋아요 등록 - 기존 좋아요가 없는 경우")
        void addLike() {
            // given
            User user = createUser();
            Product product = createProduct();

            given(userService.authenticate("user1", "password")).willReturn(user);
            given(productService.getProduct(1L)).willReturn(product);
            given(likeRepository.findByUserIdAndProductId(user.getId(), 1L)).willReturn(Optional.empty());

            // when
            likeService.toggleLike("user1", "password", 1L);

            // then
            verify(likeRepository).save(any(Like.class));
            verify(productService).increaseLikeCount(1L);
        }

        @Test
        @DisplayName("좋아요 취소 - 기존 좋아요가 있는 경우")
        void removeLike() {
            // given
            User user = createUser();
            Product product = createProduct();
            Like like = Like.create(user, product);

            given(userService.authenticate("user1", "password")).willReturn(user);
            given(productService.getProduct(1L)).willReturn(product);
            given(likeRepository.findByUserIdAndProductId(user.getId(), 1L)).willReturn(Optional.of(like));

            // when
            likeService.toggleLike("user1", "password", 1L);

            // then
            verify(likeRepository).delete(like);
            verify(productService).decreaseLikeCount(1L);
        }
    }

    @Nested
    @DisplayName("좋아요한 상품 목록 조회")
    class GetLikedProducts {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            User user = createUser();
            Product product = createProduct();
            Like like = Like.create(user, product);
            Page<Like> page = new PageImpl<>(List.of(like), PageRequest.of(0, 20), 1);

            given(userService.authenticate("user1", "password")).willReturn(user);
            given(likeRepository.findLikedProductsByUserId(user.getId(), PageRequest.of(0, 20))).willReturn(page);

            // when
            PageResponse<ProductListResponse> response = likeService.getLikedProducts("user1", "password", 0, 20);

            // then
            assertThat(response.getData()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
        }
    }
}
