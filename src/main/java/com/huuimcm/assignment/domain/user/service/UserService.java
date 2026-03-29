package com.huuimcm.assignment.domain.user.service;

import com.huuimcm.assignment.domain.user.dto.request.UserCreateRequest;
import com.huuimcm.assignment.domain.user.dto.response.UserCreateResponse;
import com.huuimcm.assignment.domain.user.dto.response.UserInfoResponse;
import com.huuimcm.assignment.domain.user.entity.User;
import com.huuimcm.assignment.domain.user.exception.UserErrorCode;
import com.huuimcm.assignment.domain.user.exception.UserException;
import com.huuimcm.assignment.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserCreateResponse signup(UserCreateRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            log.warn("회원가입 실패 - 중복된 로그인 ID: {}", request.loginId());
            throw new UserException(UserErrorCode.DUPLICATE_LOGIN_ID);
        }

        String encodedPassword = passwordEncoder.encode(request.loginPw());

        User user = User.create(
                request.loginId(),
                encodedPassword,
                request.name()
        );

        User savedUser = userRepository.save(user);
        log.info("회원가입 성공 - loginId: {}", savedUser.getLoginId());
        return UserCreateResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(String loginId, String loginPw) {
        User user = authenticate(loginId, loginPw);
        return UserInfoResponse.from(user);
    }

    @Transactional
    public void changePassword(String loginId, String loginPw, String newPassword) {
        User user = authenticate(loginId, loginPw);
        user.changePassword(passwordEncoder.encode(newPassword));
        log.info("비밀번호 변경 성공 - loginId: {}", loginId);
    }

    /**
     * X-Huuim-LoginId, X-Huuim-LoginPw 헤더를 통해 사용자를 식별합니다.
     * 인증/인가는 주요 스코프가 아니므로 인터셉터 대신 서비스 메서드로 처리하였습니다.
     */
    public User authenticate(String loginId, String loginPw) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> {
                    log.warn("인증 실패 - 존재하지 않는 유저: {}", loginId);
                    return new UserException(UserErrorCode.USER_NOT_FOUND);
                });

        if (!passwordEncoder.matches(loginPw, user.getLoginPw())) {
            log.warn("인증 실패 - 비밀번호 불일치: {}", loginId);
            throw new UserException(UserErrorCode.INVALID_PASSWORD);
        }

        return user;
    }
}
