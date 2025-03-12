package com.example.inventoryservice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryService {

    List<InventoryEntity> getAllInventories();

    InventoryEntity getInventoryOf(UUID productId);

    Boolean decrementInventoryOf(UUID productId, Long amount);

    InventoryEntity createInventory(InventoryEntity inventory);

    InventoryEntity updateInventory(InventoryEntity inventory);
}
