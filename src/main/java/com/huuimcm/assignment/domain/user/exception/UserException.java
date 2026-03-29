package com.huuimcm.assignment.domain.user.exception;

import com.huuimcm.assignment.global.exception.BusinessException;

public class UserException extends BusinessException {

    public UserException(UserErrorCode errorCode) {
        super(errorCode);
    }
}
