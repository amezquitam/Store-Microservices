package com.example.orderservice.order;

import com.example.orderservice.dto.OrderDTO;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {

    List<OrderDTO> getAllOrders();

    Optional<OrderDTO> getOrderById(UUID orderId);

    Mono<OrderDTO> createOrder(OrderDTO order);

    void deleteOrderById(UUID orderId);

    void setToken(String token);
}
