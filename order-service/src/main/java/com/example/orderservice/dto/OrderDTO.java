package com.example.orderservice.dto;

import com.example.orderservice.order_products.OrderProduct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDTO(
        UUID id,
        LocalDateTime orderDate,
        List<OrderProductDTO> orderProducts
) {
}