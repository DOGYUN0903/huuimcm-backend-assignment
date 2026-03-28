package com.huuimcm.assignment.domain.product.service;

import com.huuimcm.assignment.domain.product.dto.request.ProductCreateRequest;
import com.huuimcm.assignment.domain.product.dto.response.ProductCreateResponse;
import com.huuimcm.assignment.domain.product.dto.response.ProductListResponse;
import com.huuimcm.assignment.domain.product.entity.Product;
import com.huuimcm.assignment.domain.product.exception.ProductErrorCode;
import com.huuimcm.assignment.domain.product.exception.ProductException;
import com.huuimcm.assignment.domain.product.repository.ProductRepository;
import com.huuimcm.assignment.domain.user.entity.User;
import com.huuimcm.assignment.domain.user.service.UserService;
import com.huuimcm.assignment.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @Transactional(readOnly = true)
    public PageResponse<ProductListResponse> getProducts(String sort, int page, int size) {
        Page<Product> products = productRepository.findProducts(PageRequest.of(page, size), sort);
        return new PageResponse<>(products.map(ProductListResponse::from));
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    @Transactional
    public void increaseLikeCount(Long productId) {
        productRepository.increaseLikeCount(productId);
    }

    @Transactional
    public void decreaseLikeCount(Long productId) {
        productRepository.decreaseLikeCount(productId);
    }
}
