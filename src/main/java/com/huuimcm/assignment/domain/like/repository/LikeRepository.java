package com.huuimcm.assignment.domain.like.repository;

import com.huuimcm.assignment.domain.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long>, LikeRepositoryCustom {

    Optional<Like> findByUserIdAndProductId(Long userId, Long productId);
}
