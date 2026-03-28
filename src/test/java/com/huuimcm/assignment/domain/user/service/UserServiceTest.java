package com.huuimcm.assignment.domain.user.service;

import com.huuimcm.assignment.domain.user.dto.request.UserCreateRequest;
import com.huuimcm.assignment.domain.user.dto.response.UserCreateResponse;
import com.huuimcm.assignment.domain.user.dto.response.UserInfoResponse;
import com.huuimcm.assignment.domain.user.entity.User;
import com.huuimcm.assignment.domain.user.exception.UserException;
import com.huuimcm.assignment.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("회원가입")
    class Signup {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            UserCreateRequest request = new UserCreateRequest("testuser", "password123", "테스트유저");
            User user = User.create("testuser", "encodedPassword", "테스트유저");

            given(userRepository.existsByLoginId("testuser")).willReturn(false);
            given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willReturn(user);

            // when
            UserCreateResponse response = userService.signup(request);

            // then
            assertThat(response.loginId()).isEqualTo("testuser");
            assertThat(response.name()).isEqualTo("테스트유저");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("실패 - 중복된 로그인 ID")
        void fail_duplicateLoginId() {
            // given
            UserCreateRequest request = new UserCreateRequest("testuser", "password123", "테스트유저");
            given(userRepository.existsByLoginId("testuser")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.signup(request))
                    .isInstanceOf(UserException.class);
        }
    }

    @Nested
    @DisplayName("내 정보 조회")
    class GetMyInfo {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            User user = User.create("testuser", "encodedPassword", "테스트유저");

            given(userRepository.findByLoginId("testuser")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);

            // when
            UserInfoResponse response = userService.getMyInfo("testuser", "password123");

            // then
            assertThat(response.loginId()).isEqualTo("testuser");
            assertThat(response.name()).isEqualTo("테스트유저");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 유저")
        void fail_userNotFound() {
            // given
            given(userRepository.findByLoginId("unknown")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getMyInfo("unknown", "password123"))
                    .isInstanceOf(UserException.class);
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void fail_invalidPassword() {
            // given
            User user = User.create("testuser", "encodedPassword", "테스트유저");

            given(userRepository.findByLoginId("testuser")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.getMyInfo("testuser", "wrongPassword"))
                    .isInstanceOf(UserException.class);
        }
    }

    @Nested
    @DisplayName("비밀번호 변경")
    class ChangePassword {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            User user = User.create("testuser", "encodedPassword", "테스트유저");

            given(userRepository.findByLoginId("testuser")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
            given(passwordEncoder.encode("newPassword")).willReturn("encodedNewPassword");

            // when
            userService.changePassword("testuser", "password123", "newPassword");

            // then
            assertThat(user.getLoginPw()).isEqualTo("encodedNewPassword");
        }

        @Test
        @DisplayName("실패 - 인증 실패 시 비밀번호 변경 불가")
        void fail_authenticationFailed() {
            // given
            given(userRepository.findByLoginId("unknown")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.changePassword("unknown", "password123", "newPassword"))
                    .isInstanceOf(UserException.class);
        }
    }

    @Nested
    @DisplayName("인증")
    class Authenticate {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            User user = User.create("testuser", "encodedPassword", "테스트유저");

            given(userRepository.findByLoginId("testuser")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);

            // when
            User result = userService.authenticate("testuser", "password123");

            // then
            assertThat(result.getLoginId()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 유저")
        void fail_userNotFound() {
            // given
            given(userRepository.findByLoginId("unknown")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.authenticate("unknown", "password123"))
                    .isInstanceOf(UserException.class);
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void fail_invalidPassword() {
            // given
            User user = User.create("testuser", "encodedPassword", "테스트유저");

            given(userRepository.findByLoginId("testuser")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.authenticate("testuser", "wrongPassword"))
                    .isInstanceOf(UserException.class);
        }
    }
}
