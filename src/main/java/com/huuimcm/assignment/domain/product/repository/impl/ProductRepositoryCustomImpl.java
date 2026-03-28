package com.huuimcm.assignment.domain.product.repository.impl;

import com.huuimcm.assignment.domain.product.entity.Product;
import com.huuimcm.assignment.domain.product.repository.ProductRepositoryCustom;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.huuimcm.assignment.domain.product.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> findProducts(Pageable pageable, String sortBy) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .orderBy(getOrderSpecifier(sortBy))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public void increaseLikeCount(Long productId) {
        queryFactory
                .update(product)
                .set(product.likeCount, product.likeCount.add(1))
                .where(product.id.eq(productId))
                .execute();
    }

    @Override
    public void decreaseLikeCount(Long productId) {
        queryFactory
                .update(product)
                .set(product.likeCount, product.likeCount.subtract(1))
                .where(product.id.eq(productId), product.likeCount.gt(0))
                .execute();
    }

    private OrderSpecifier<?> getOrderSpecifier(String sortBy) {
        return switch (sortBy) {
            case "price_asc" -> product.price.asc();
            case "likes_desc" -> product.likeCount.desc();
            default -> product.createdAt.desc();
        };
    }
}
