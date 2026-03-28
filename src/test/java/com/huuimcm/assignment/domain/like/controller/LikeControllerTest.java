package com.huuimcm.assignment.domain.like.controller;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @BeforeEach
    void setUp() {
        userService.signup(new UserCreateRequest("user1", "password123", "테스트유저"));
    }

    private Long createProduct() {
        ProductCreateResponse response = productService.create("user1", "password123",
                new ProductCreateRequest("나이키 에어맥스", "운동화 설명", 159000L, 100, "나이키"));
        return response.id();
    }

    @Nested
    @DisplayName("POST /api/v1/products/{productId}/likes - 좋아요 토글")
    class ToggleLike {

        @Test
        @DisplayName("성공 - 좋아요 등록")
        void success_addLike() throws Exception {
            Long productId = createProduct();

            mockMvc.perform(post("/api/v1/products/{productId}/likes", productId)
                            .header("X-Huuim-LoginId", "user1")
                            .header("X-Huuim-LoginPw", "password123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("좋아요가 처리되었습니다"));
        }

        @Test
        @DisplayName("성공 - 좋아요 취소")
        void success_removeLike() throws Exception {
            Long productId = createProduct();

            // 좋아요 등록
            mockMvc.perform(post("/api/v1/products/{productId}/likes", productId)
                    .header("X-Huuim-LoginId", "user1")
                    .header("X-Huuim-LoginPw", "password123"));

            // 좋아요 취소
            mockMvc.perform(post("/api/v1/products/{productId}/likes", productId)
                            .header("X-Huuim-LoginId", "user1")
                            .header("X-Huuim-LoginPw", "password123"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("실패 - 인증 실패")
        void fail_unauthorized() throws Exception {
            Long productId = createProduct();

            mockMvc.perform(post("/api/v1/products/{productId}/likes", productId)
                            .header("X-Huuim-LoginId", "user1")
                            .header("X-Huuim-LoginPw", "wrongPassword"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/liked - 좋아요한 상품 목록 조회")
    class GetLikedProducts {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            Long productId = createProduct();

            // 좋아요 등록
            mockMvc.perform(post("/api/v1/products/{productId}/likes", productId)
                    .header("X-Huuim-LoginId", "user1")
                    .header("X-Huuim-LoginPw", "password123"));

            mockMvc.perform(get("/api/v1/products/liked")
                            .header("X-Huuim-LoginId", "user1")
                            .header("X-Huuim-LoginPw", "password123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.data.length()").value(1))
                    .andExpect(jsonPath("$.data.data[0].name").value("나이키 에어맥스"));
        }

        @Test
        @DisplayName("성공 - 좋아요한 상품이 없는 경우")
        void success_empty() throws Exception {
            mockMvc.perform(get("/api/v1/products/liked")
                            .header("X-Huuim-LoginId", "user1")
                            .header("X-Huuim-LoginPw", "password123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.data.length()").value(0));
        }
    }
}
