package com.huuimcm.assignment.domain.order.exception;

import com.huuimcm.assignment.global.exception.BusinessException;

public class OrderException extends BusinessException {

    public OrderException(OrderErrorCode errorCode) {
        super(errorCode);
    }
}
