package com.huuimcm.assignment.domain.product.service;

import com.huuimcm.assignment.domain.product.dto.request.ProductCreateRequest;
import com.huuimcm.assignment.domain.product.dto.response.ProductCreateResponse;
import com.huuimcm.assignment.domain.product.entity.Product;
import com.huuimcm.assignment.domain.product.repository.ProductRepository;
import com.huuimcm.assignment.domain.user.entity.User;
import com.huuimcm.assignment.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserService userService;

    @Transactional
    public ProductCreateResponse create(String loginId, String loginPw, ProductCreateRequest request) {
        User seller = userService.authenticate(loginId, loginPw);

        Product product = Product.create(
                seller,
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                request.brand()
        );

        Product savedProduct = productRepository.save(product);
        return ProductCreateResponse.from(savedProduct);
    }
}
