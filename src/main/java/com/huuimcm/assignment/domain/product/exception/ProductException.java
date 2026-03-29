package com.huuimcm.assignment.domain.product.exception;

import com.huuimcm.assignment.global.exception.BusinessException;

public class ProductException extends BusinessException {

    public ProductException(ProductErrorCode errorCode) {
        super(errorCode);
    }
}
