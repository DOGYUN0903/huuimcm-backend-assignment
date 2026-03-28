package com.huuimcm.assignment.domain.product.repository;

import com.huuimcm.assignment.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

    Page<Product> findProducts(Pageable pageable, String sortBy);
}
