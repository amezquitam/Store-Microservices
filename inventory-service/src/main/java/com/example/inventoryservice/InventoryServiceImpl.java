package com.example.inventoryservice;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public List<InventoryEntity> getAllInventories() {
        return inventoryRepository.findAll();
    }

    @Override
    public InventoryEntity getInventoryOf(UUID productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory with given product id not found"));
    }

    @Override
    public void decrementInventoryOf(UUID productId, Long amount) {
        int success = inventoryRepository.decrementStock(productId, amount);

        if (success != 1) {
            var inventory = inventoryRepository.findByProductId(productId).orElseThrow(() -> new RuntimeException("Inventory of product not found"));
            if (inventory.getStock() < amount) {
                throw new RuntimeException("Insufficient stock");
            }
        }
    }

    @Override
    public InventoryEntity createInventory(InventoryEntity inventory) {
        return inventoryRepository.save(inventory);
    }

    @Override
    public InventoryEntity updateInventory(InventoryEntity inventory) {
        if (inventory.getInventoryId() == null) {
            throw new RuntimeException("Inventory ID is required");
        }

        inventoryRepository.findById(inventory.getInventoryId()).orElseThrow(
                () -> new RuntimeException("Inventory does not exist")
        );

        return inventoryRepository.save(inventory);
    }
}
