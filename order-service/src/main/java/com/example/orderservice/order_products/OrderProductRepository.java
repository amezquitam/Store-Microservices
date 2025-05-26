package com.example.orderservice.order_products;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, UUID> {
    
}
