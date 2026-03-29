package com.huuimcm.assignment.domain.user.controller;

import com.huuimcm.assignment.domain.user.dto.request.PasswordChangeRequest;
import com.huuimcm.assignment.domain.user.dto.request.UserCreateRequest;
import com.huuimcm.assignment.domain.user.service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Nested
    @DisplayName("POST /api/v1/users - 회원가입")
    class Signup {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            UserCreateRequest request = new UserCreateRequest("testuser", "password123", "테스트유저");

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다"))
                    .andExpect(jsonPath("$.data.loginId").value("testuser"))
                    .andExpect(jsonPath("$.data.name").value("테스트유저"));
        }

        @Test
        @DisplayName("실패 - 로그인 ID 누락")
        void fail_blankLoginId() throws Exception {
            UserCreateRequest request = new UserCreateRequest("", "password123", "테스트유저");

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 중복된 로그인 ID")
        void fail_duplicateLoginId() throws Exception {
            userService.signup(new UserCreateRequest("testuser", "password123", "테스트유저"));

            UserCreateRequest request = new UserCreateRequest("testuser", "password456", "다른유저");

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/me - 내 정보 조회")
    class GetMyInfo {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            userService.signup(new UserCreateRequest("testuser", "password123", "테스트유저"));

            mockMvc.perform(get("/api/v1/users/me")
                            .header("X-Huuim-LoginId", "testuser")
                            .header("X-Huuim-LoginPw", "password123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.loginId").value("testuser"))
                    .andExpect(jsonPath("$.data.name").value("테스트유저"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 유저")
        void fail_userNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/users/me")
                            .header("X-Huuim-LoginId", "unknown")
                            .header("X-Huuim-LoginPw", "password123"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void fail_invalidPassword() throws Exception {
            userService.signup(new UserCreateRequest("testuser", "password123", "테스트유저"));

            mockMvc.perform(get("/api/v1/users/me")
                            .header("X-Huuim-LoginId", "testuser")
                            .header("X-Huuim-LoginPw", "wrongPassword"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/users/me/password - 비밀번호 변경")
    class ChangePassword {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            userService.signup(new UserCreateRequest("testuser", "password123", "테스트유저"));

            PasswordChangeRequest request = new PasswordChangeRequest("newPassword");

            mockMvc.perform(put("/api/v1/users/me/password")
                            .header("X-Huuim-LoginId", "testuser")
                            .header("X-Huuim-LoginPw", "password123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("비밀번호가 변경되었습니다"));
        }

        @Test
        @DisplayName("실패 - 새 비밀번호 누락")
        void fail_blankNewPassword() throws Exception {
            userService.signup(new UserCreateRequest("testuser", "password123", "테스트유저"));

            PasswordChangeRequest request = new PasswordChangeRequest("");

            mockMvc.perform(put("/api/v1/users/me/password")
                            .header("X-Huuim-LoginId", "testuser")
                            .header("X-Huuim-LoginPw", "password123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
