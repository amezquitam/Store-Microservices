package com.example.inventoryservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<InventoryEntity, UUID> {


    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE InventoryEntity i SET i.stock = (i.stock - :amount) WHERE i.inventoryId = :inventoryId AND i.stock >= :amount")
    int decrementStock(UUID inventoryId, Long amount);

    Optional<InventoryEntity> findByProductId(UUID uuid);
}