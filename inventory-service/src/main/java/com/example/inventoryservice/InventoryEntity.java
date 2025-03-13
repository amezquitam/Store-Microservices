package com.example.inventoryservice;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class InventoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID inventoryId;

    private UUID productId;

    @Min(0)
    private Long stock;

    @Min(0)
    private Long minimumQuantity;

    @Min(0)
    private Long maximumQuantity;
}
