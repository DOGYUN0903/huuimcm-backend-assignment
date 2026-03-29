package com.huuimcm.assignment.domain.product.repository;

import com.huuimcm.assignment.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepositoryCustom {

    Page<Product> findProducts(Pageable pageable, String sortBy);

    void increaseLikeCount(Long productId);

    void decreaseLikeCount(Long productId);

    Optional<Product> findByIdWithPessimisticLock(Long productId);
}
