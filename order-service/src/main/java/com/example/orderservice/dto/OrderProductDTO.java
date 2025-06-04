package com.example.orderservice.dto;

import java.util.UUID;

public record OrderProductDTO (
     UUID orderId,
     UUID productId,
     Long quantity
)
{}

