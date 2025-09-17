package com.restaurant.orders.repository;

import com.restaurant.orders.entity.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderEventRepository extends JpaRepository<OrderEvent, String> {
    List<OrderEvent> findByOrderIdOrderByCreatedAtAsc(String orderId);
}