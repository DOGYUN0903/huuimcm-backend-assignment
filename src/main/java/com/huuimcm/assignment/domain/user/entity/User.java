package com.huuimcm.assignment.domain.user.entity;

import com.huuimcm.assignment.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false, length = 255)
    private String loginPw;

    @Column(nullable = false, length = 50)
    private String name;

    private User(String loginId, String loginPw, String name) {
        this.loginId = loginId;
        this.loginPw = loginPw;
        this.name = name;
    }

    public static User create(String loginId, String loginPw, String name) {
        return new User(loginId, loginPw, name);
    }

    public void changePassword(String encodedPassword) {
        this.loginPw = encodedPassword;
    }
}
