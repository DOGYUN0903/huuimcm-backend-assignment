package com.huuimcm.assignment.domain.like.repository.impl;

import com.huuimcm.assignment.domain.like.entity.Like;
import com.huuimcm.assignment.domain.like.repository.LikeRepositoryCustom;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.huuimcm.assignment.domain.like.entity.QLike.like;
import static com.huuimcm.assignment.domain.product.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class LikeRepositoryCustomImpl implements LikeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Like> findLikedProductsByUserId(Long userId, Pageable pageable) {
        List<Like> content = queryFactory
                .selectFrom(like)
                .join(like.product, product).fetchJoin()
                .where(like.user.id.eq(userId))
                .orderBy(like.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(like.count())
                .from(like)
                .where(like.user.id.eq(userId));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
