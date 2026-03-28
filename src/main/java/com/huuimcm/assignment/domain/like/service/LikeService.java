package com.huuimcm.assignment.domain.like.service;

import com.huuimcm.assignment.domain.like.entity.Like;
import com.huuimcm.assignment.domain.like.repository.LikeRepository;
import com.huuimcm.assignment.domain.product.entity.Product;
import com.huuimcm.assignment.domain.product.service.ProductService;
import com.huuimcm.assignment.domain.user.entity.User;
import com.huuimcm.assignment.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserService userService;
    private final ProductService productService;

    @Transactional
    public void toggleLike(String loginId, String loginPw, Long productId) {
        User user = userService.authenticate(loginId, loginPw);
        Product product = productService.getProduct(productId);

        Optional<Like> existingLike = likeRepository.findByUserIdAndProductId(user.getId(), productId);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            productService.decreaseLikeCount(productId);
        } else {
            likeRepository.save(Like.create(user, product));
            productService.increaseLikeCount(productId);
        }
    }
}
