package com.example.orderservice.dto;

import lombok.Builder;
import lombok.With;
import java.time.LocalDateTime;
import java.util.UUID;

@With
@Builder
public record OrderDTO(
        UUID id,
        LocalDateTime orderDate,
        String status,
        Double totalAmount
) {
}