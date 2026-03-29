package com.huuimcm.assignment.domain.like.repository;

import com.huuimcm.assignment.domain.like.entity.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LikeRepositoryCustom {

    Page<Like> findLikedProductsByUserId(Long userId, Pageable pageable);
}
