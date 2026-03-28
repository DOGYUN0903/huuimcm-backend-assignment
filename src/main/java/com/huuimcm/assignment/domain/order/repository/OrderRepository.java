package com.huuimcm.assignment.domain.order.repository;

import com.huuimcm.assignment.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);
}
