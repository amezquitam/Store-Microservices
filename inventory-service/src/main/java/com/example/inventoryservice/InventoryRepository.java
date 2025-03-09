package com.example.inventoryservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<InventoryEntity, UUID> {


    @Modifying
    @Transactional
    @Query("UPDATE InventoryEntity i SET i.stock = i.stock - :amount WHERE i.productId = :productId AND i.stock >= :amount")
    int decrementStock(UUID productId, Long amount);


    Optional<InventoryEntity> findByProductId(UUID uuid);
}