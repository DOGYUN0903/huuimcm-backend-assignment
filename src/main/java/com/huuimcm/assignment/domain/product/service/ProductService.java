package com.huuimcm.assignment.domain.product.service;

import com.huuimcm.assignment.domain.product.dto.request.ProductCreateRequest;
import com.huuimcm.assignment.domain.product.dto.response.ProductCreateResponse;
import com.huuimcm.assignment.domain.product.dto.response.ProductDetailResponse;
import com.huuimcm.assignment.domain.product.dto.response.ProductListResponse;
import com.huuimcm.assignment.domain.product.entity.Product;
import com.huuimcm.assignment.domain.product.exception.ProductErrorCode;
import com.huuimcm.assignment.domain.product.exception.ProductException;
import com.huuimcm.assignment.domain.product.repository.ProductRepository;
import com.huuimcm.assignment.domain.user.entity.User;
import com.huuimcm.assignment.domain.user.service.UserService;
import com.huuimcm.assignment.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserService userService;

    @CacheEvict(value = "products", allEntries = true)
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
        log.info("상품 등록 성공 - productId: {}, name: {}, seller: {}", savedProduct.getId(), savedProduct.getName(), loginId);
        return ProductCreateResponse.from(savedProduct);
    }

    @Cacheable(value = "products", key = "#sort + '_' + #page + '_' + #size")
    @Transactional(readOnly = true)
    public PageResponse<ProductListResponse> getProducts(String sort, int page, int size) {
        Page<Product> products = productRepository.findProducts(PageRequest.of(page, size), sort);
        return new PageResponse<>(products.map(ProductListResponse::from));
    }

    @Cacheable(value = "product", key = "#productId")
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
        return ProductDetailResponse.from(product);
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    @Transactional
    public Product getProductWithLock(Long productId) {
        return productRepository.findByIdWithPessimisticLock(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    @CacheEvict(value = "product", key = "#productId")
    @Transactional
    public void increaseLikeCount(Long productId) {
        productRepository.increaseLikeCount(productId);
    }

    @CacheEvict(value = "product", key = "#productId")
    @Transactional
    public void decreaseLikeCount(Long productId) {
        productRepository.decreaseLikeCount(productId);
    }
}
