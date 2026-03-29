package com.huuimcm.assignment.domain.order.repository.impl;

import com.huuimcm.assignment.domain.order.entity.Order;
import com.huuimcm.assignment.domain.order.repository.OrderRepositoryCustom;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.huuimcm.assignment.domain.order.entity.QOrder.order;
import static com.huuimcm.assignment.domain.order.entity.QOrderItem.orderItem;
import static com.huuimcm.assignment.domain.product.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Order> findOrdersByUserId(Long userId, Pageable pageable) {
        List<Long> orderIds = queryFactory
                .select(order.id)
                .from(order)
                .where(order.user.id.eq(userId))
                .orderBy(order.createdAt.desc(), order.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(order.count())
                .from(order)
                .where(order.user.id.eq(userId));

        if (orderIds.isEmpty()) {
            return PageableExecutionUtils.getPage(List.of(), pageable, countQuery::fetchOne);
        }

        List<Order> content = queryFactory
                .selectDistinct(order)
                .from(order)
                .join(order.orderItems, orderItem).fetchJoin()
                .join(orderItem.product, product).fetchJoin()
                .where(order.id.in(orderIds))
                .orderBy(order.createdAt.desc(), order.id.desc())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
