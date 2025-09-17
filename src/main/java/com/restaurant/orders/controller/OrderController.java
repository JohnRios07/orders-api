package com.restaurant.orders.controller;

import com.restaurant.orders.dto.OrderDto;
import com.restaurant.orders.entity.OrderEvent;
import com.restaurant.orders.entity.OrderStatus;
import com.restaurant.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "Get orders with filters")
    public ResponseEntity<Page<OrderDto>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDto> orders = orderService.getOrders(status, customerName, phone, startDate, endDate, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderDto> getOrder(@PathVariable String id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new order")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderDto orderDto) {
        OrderDto created = orderService.createOrder(orderDto);
        return ResponseEntity.ok(created);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable String id, @RequestBody Map<String, Object> request) {
        OrderStatus newStatus = OrderStatus.valueOf((String) request.get("to"));
        String notes = (String) request.get("notes");
        OrderDto updated = orderService.updateOrderStatus(id, newStatus, notes);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update order details")
    public ResponseEntity<OrderDto> updateOrder(@PathVariable String id, @RequestBody OrderDto orderDto) {
        OrderDto updated = orderService.updateOrder(id, orderDto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/timeline")
    @Operation(summary = "Get order timeline")
    public ResponseEntity<List<OrderEvent>> getOrderTimeline(@PathVariable String id) {
        List<OrderEvent> timeline = orderService.getOrderTimeline(id);
        return ResponseEntity.ok(timeline);
    }
}