package com.example.orderservice.order_products;

import com.example.orderservice.order.Order;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@Table(name = "order_product")
@NoArgsConstructor
@AllArgsConstructor
public class OrderProduct {
    @Id
    private UUID id;
    private UUID productId;
    private Long quantity;
    @ManyToOne
    private Order order;
}
