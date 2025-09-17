package com.restaurant.orders.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.orders.entity.*;
import com.restaurant.orders.repository.OrderEventRepository;
import com.restaurant.orders.repository.OrderRepository;
import com.restaurant.orders.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventRepository orderEventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private String orderId;

    @BeforeEach
    void setUp() {
        orderId = "test-order-id";
        order = new Order();
        order.setId(orderId);
        order.setCustomerName("Test Customer");
        order.setStatus(OrderStatus.RECIBIDO);
    }

    @Test
    void shouldUpdateOrderStatusSuccessfully() {
        // Given
        OrderStatus newStatus = OrderStatus.PREPARANDO;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderEventRepository.save(any(OrderEvent.class))).thenReturn(new OrderEvent());

        // When
        var result = orderService.updateOrderStatus(orderId, newStatus, null);

        // Then
        assertThat(result.getStatus()).isEqualTo(newStatus);
        verify(orderRepository).save(order);
        verify(orderEventRepository).save(any(OrderEvent.class));
    }

    @Test
    void shouldThrowExceptionForInvalidStatusTransition() {
        // Given
        order.setStatus(OrderStatus.ENTREGADO);
        OrderStatus invalidStatus = OrderStatus.RECIBIDO;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, invalidStatus, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid status transition");
    }

    @Test
    void shouldGetOrdersWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(order));
        when(orderRepository.findWithFilters(null, null, null, null, null, pageable)).thenReturn(orderPage);

        // When
        var result = orderService.getOrders(null, null, null, null, null, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findWithFilters(null, null, null, null, null, pageable);
    }
}