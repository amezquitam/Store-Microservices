package com.example.orderservice.order;

import com.example.orderservice.order_products.OrderProduct;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime timestamp;

    @OneToMany
    private List<OrderProduct> orderProducts;

}