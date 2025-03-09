package com.example.inventoryservice;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class InventoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID inventoryId;

    @Column(unique = true, nullable = false)
    private UUID productId;

    private Long stock;

    private Long minimumQuantity;

    private Long maximumQuantity;
}
