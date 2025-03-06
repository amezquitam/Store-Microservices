package com.example.orderservice;

import com.example.orderservice.dto.OrderDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {

    List<OrderDTO> getAllOrders();

    Optional<OrderDTO> getOrderById(UUID orderId);

    OrderDTO createOrder(OrderDTO order);

    void deleteOrderById(UUID orderId);
}
