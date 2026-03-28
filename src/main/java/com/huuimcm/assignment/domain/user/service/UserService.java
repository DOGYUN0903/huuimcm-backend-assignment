package com.huuimcm.assignment.domain.user.service;

import com.huuimcm.assignment.domain.user.dto.request.UserCreateRequest;
import com.huuimcm.assignment.domain.user.dto.response.UserCreateResponse;
import com.huuimcm.assignment.domain.user.entity.User;
import com.huuimcm.assignment.domain.user.exception.UserErrorCode;
import com.huuimcm.assignment.domain.user.exception.UserException;
import com.huuimcm.assignment.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserCreateResponse signup(UserCreateRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new UserException(UserErrorCode.DUPLICATE_LOGIN_ID);
        }

        String encodedPassword = passwordEncoder.encode(request.loginPw());

        User user = User.create(
                request.loginId(),
                encodedPassword,
                request.name()
        );

        User savedUser = userRepository.save(user);
        return UserCreateResponse.from(savedUser);
    }
}
