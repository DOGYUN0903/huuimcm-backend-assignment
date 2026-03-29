package com.huuimcm.assignment.domain.product.controller;

import com.huuimcm.assignment.domain.product.dto.request.ProductCreateRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerTest {

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
        userService.signup(new UserCreateRequest("seller", "password123", "판매자"));
    }

    @Nested
    @DisplayName("POST /api/v1/products - 상품 등록")
    class Create {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest("나이키 에어맥스", "운동화 설명", 159000L, 100, "나이키");

            mockMvc.perform(post("/api/v1/products")
                            .header("X-Huuim-LoginId", "seller")
                            .header("X-Huuim-LoginPw", "password123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.name").value("나이키 에어맥스"))
                    .andExpect(jsonPath("$.data.brand").value("나이키"));
        }

        @Test
        @DisplayName("실패 - 상품명 누락")
        void fail_blankName() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest("", "운동화 설명", 159000L, 100, "나이키");

            mockMvc.perform(post("/api/v1/products")
                            .header("X-Huuim-LoginId", "seller")
                            .header("X-Huuim-LoginPw", "password123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 인증 실패")
        void fail_unauthorized() throws Exception {
            ProductCreateRequest request = new ProductCreateRequest("나이키 에어맥스", "운동화 설명", 159000L, 100, "나이키");

            mockMvc.perform(post("/api/v1/products")
                            .header("X-Huuim-LoginId", "seller")
                            .header("X-Huuim-LoginPw", "wrongPassword")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products - 상품 목록 조회")
    class GetProducts {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            productService.create("seller", "password123", new ProductCreateRequest("나이키 에어맥스", "설명", 159000L, 100, "나이키"));
            productService.create("seller", "password123", new ProductCreateRequest("아디다스 삼바", "설명", 129000L, 90, "아디다스"));

            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.data.length()").value(2));
        }

        @Test
        @DisplayName("성공 - 가격 오름차순 정렬")
        void success_sortByPriceAsc() throws Exception {
            productService.create("seller", "password123", new ProductCreateRequest("비싼 상품", "설명", 200000L, 10, "브랜드A"));
            productService.create("seller", "password123", new ProductCreateRequest("싼 상품", "설명", 100000L, 10, "브랜드B"));

            mockMvc.perform(get("/api/v1/products")
                            .param("sort", "price_asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.data[0].name").value("싼 상품"))
                    .andExpect(jsonPath("$.data.data[1].name").value("비싼 상품"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{productId} - 상품 상세 조회")
    class GetProduct {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            var created = productService.create("seller", "password123",
                    new ProductCreateRequest("나이키 에어맥스", "운동화 설명", 159000L, 100, "나이키"));

            mockMvc.perform(get("/api/v1/products/{productId}", created.id()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("나이키 에어맥스"))
                    .andExpect(jsonPath("$.data.description").value("운동화 설명"))
                    .andExpect(jsonPath("$.data.sellerName").value("판매자"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상품")
        void fail_notFound() throws Exception {
            mockMvc.perform(get("/api/v1/products/999"))
                    .andExpect(status().isNotFound());
        }
    }
}
