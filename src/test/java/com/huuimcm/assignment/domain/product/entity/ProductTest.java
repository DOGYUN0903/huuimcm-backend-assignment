package com.huuimcm.assignment.domain.product.entity;

import com.huuimcm.assignment.domain.product.exception.ProductException;
import com.huuimcm.assignment.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private User createSeller() {
        return User.create("seller", "encodedPw", "판매자");
    }

    @Nested
    @DisplayName("재고 차감")
    class DecreaseStock {

        @Test
        @DisplayName("성공 - 재고가 충분한 경우")
        void success() {
            // given
            Product product = Product.create(createSeller(), "상품", "설명", 10000L, 10, "브랜드");

            // when
            product.decreaseStock(3);

            // then
            assertThat(product.getStock()).isEqualTo(7);
        }

        @Test
        @DisplayName("성공 - 재고를 전부 소진하는 경우")
        void success_allStock() {
            // given
            Product product = Product.create(createSeller(), "상품", "설명", 10000L, 5, "브랜드");

            // when
            product.decreaseStock(5);

            // then
            assertThat(product.getStock()).isEqualTo(0);
        }

        @Test
        @DisplayName("실패 - 재고 부족")
        void fail_insufficientStock() {
            // given
            Product product = Product.create(createSeller(), "상품", "설명", 10000L, 3, "브랜드");

            // when & then
            assertThatThrownBy(() -> product.decreaseStock(5))
                    .isInstanceOf(ProductException.class);
        }
    }
}
