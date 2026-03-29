package com.huuimcm.assignment.domain.order.repository;

import com.huuimcm.assignment.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {

    Page<Order> findOrdersByUserId(Long userId, Pageable pageable);
}
