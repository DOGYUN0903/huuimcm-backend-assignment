package com.huuimcm.assignment.domain.order.repository;

import com.huuimcm.assignment.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

    Optional<Order> findByIdempotencyKey(String idempotencyKey);
}
