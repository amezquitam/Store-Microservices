package com.example.orderservice.dto;

import com.example.orderservice.order_products.OrderProduct;
import lombok.Builder;
import lombok.With;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@With
@Builder
public record OrderDTO(
        UUID id,
        LocalDateTime orderDate,
        List<OrderProduct> orderProducts
) {
}