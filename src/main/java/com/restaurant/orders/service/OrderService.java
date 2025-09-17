package com.restaurant.orders.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.orders.dto.OrderDto;
import com.restaurant.orders.dto.OrderItem;
import com.restaurant.orders.entity.*;
import com.restaurant.orders.repository.OrderEventRepository;
import com.restaurant.orders.repository.OrderRepository;
import com.restaurant.orders.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventRepository orderEventRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public OrderService(OrderRepository orderRepository, OrderEventRepository orderEventRepository,
                        UserRepository userRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderEventRepository = orderEventRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public Page<OrderDto> getOrders(OrderStatus status, String customerName, String phone,
                                    LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Order> orders = orderRepository.findWithFilters(status, customerName, phone, startDate, endDate, pageable);
        return orders.map(this::convertToDto);
    }

    public Optional<OrderDto> getOrderById(String id) {
        return orderRepository.findById(id).map(this::convertToDto);
    }

    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
        Order order = convertToEntity(orderDto);
        order.setStatus(OrderStatus.RECIBIDO);
        Order savedOrder = orderRepository.save(order);

        // Log event
        logOrderEvent(savedOrder, null, savedOrder.getStatus(), "Order created");

        return convertToDto(savedOrder);
    }

    @Transactional
    public OrderDto updateOrderStatus(String id, OrderStatus newStatus, String notes) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!isValidTransition(order.getStatus(), newStatus)) {
            throw new RuntimeException("Invalid status transition");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        // Log event
        logOrderEvent(savedOrder, oldStatus, newStatus, notes);

        return convertToDto(savedOrder);
    }

    @Transactional
    public OrderDto updateOrder(String id, OrderDto orderDto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Update allowed fields
        order.setAddress(orderDto.getAddress());
        order.setNotes(orderDto.getNotes());
        order.setPaymentMethod(orderDto.getPaymentMethod());

        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        if (from == to) return true;
        if (to == OrderStatus.CANCELADO) return true; // Can cancel from any status

        switch (from) {
            case RECIBIDO: return to == OrderStatus.PREPARANDO;
            case PREPARANDO: return to == OrderStatus.LISTO;
            case LISTO: return to == OrderStatus.DESPACHADO;
            case DESPACHADO: return to == OrderStatus.ENTREGADO;
            default: return false;
        }
    }

    private void logOrderEvent(Order order, OrderStatus fromStatus, OrderStatus toStatus, String notes) {
        User currentUser = getCurrentUser();
        OrderEvent event = new OrderEvent(order, fromStatus, toStatus, notes, currentUser);
        orderEventRepository.save(event);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            String email = ((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername();
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setCustomerName(order.getCustomerName());
        dto.setPhone(order.getPhone());
        dto.setSubtotal(order.getSubtotal());
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setTotal(order.getTotal());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setAddress(order.getAddress());
        dto.setNotes(order.getNotes());
        dto.setStatus(order.getStatus());

        // Parse items JSON
        try {
            List<OrderItem> items = objectMapper.readValue(order.getItems(), new TypeReference<List<OrderItem>>() {});
            dto.setItems(items);
        } catch (Exception e) {
            dto.setItems(List.of());
        }

        return dto;
    }

    private Order convertToEntity(OrderDto dto) {
        Order order = new Order();
        order.setCustomerName(dto.getCustomerName());
        order.setPhone(dto.getPhone());
        order.setSubtotal(dto.getSubtotal());
        order.setDeliveryFee(dto.getDeliveryFee());
        order.setTotal(dto.getTotal());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setAddress(dto.getAddress());
        order.setNotes(dto.getNotes());

        // Convert items to JSON
        try {
            String itemsJson = objectMapper.writeValueAsString(dto.getItems());
            order.setItems(itemsJson);
        } catch (Exception e) {
            order.setItems("[]");
        }

        return order;
    }

    public List<OrderEvent> getOrderTimeline(String orderId) {
        return orderEventRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
    }
}