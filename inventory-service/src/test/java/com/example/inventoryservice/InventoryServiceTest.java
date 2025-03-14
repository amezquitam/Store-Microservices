package com.example.inventoryservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest {
    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllInventories_ReturnsList() {
        // Given
        List<InventoryEntity> mockInventories = List.of(new InventoryEntity(), new InventoryEntity());
        when(inventoryRepository.findAll()).thenReturn(mockInventories);

        // When
        List<InventoryEntity> result = inventoryService.getAllInventories();

        // Then
        assertEquals(mockInventories, result);
        verify(inventoryRepository, times(1)).findAll();
    }

    @Test
    void testGetInventoryOf_ReturnsInventory() {
        // Given
        UUID productId = UUID.randomUUID();
        InventoryEntity mockInventory = new InventoryEntity();
        mockInventory.setProductId(productId);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(mockInventory));

        // When
        InventoryEntity result = inventoryService.getInventoryOf(productId);

        // Then
        assertEquals(mockInventory, result);
        verify(inventoryRepository, times(1)).findByProductId(productId);
    }

    @Test
    void testGetInventoryOf_ThrowsException_WhenInventoryNotFound() {
        // Given
        UUID productId = UUID.randomUUID();
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(RuntimeException.class, () -> inventoryService.getInventoryOf(productId));
        verify(inventoryRepository, times(1)).findByProductId(productId);
    }

    @Test
    void testDecrementInventoryOf_DecreasesStock_WhenStockIsSufficient() {
        // Given
        UUID productId = UUID.randomUUID();
        Long amount = 5L;
        when(inventoryRepository.decrementStock(productId, amount)).thenReturn(1);

        // When
        inventoryService.decrementInventoryOf(productId, amount);

        // Then
        verify(inventoryRepository, times(1)).decrementStock(productId, amount);
    }

    @Test
    void testDecrementInventoryOf_ThrowsException_WhenInsufficientStock() {
        // Given
        UUID productId = UUID.randomUUID();
        Long amount = 5L;
        InventoryEntity mockInventory = new InventoryEntity();
        mockInventory.setStock(3L);

        when(inventoryRepository.decrementStock(productId, amount)).thenReturn(0);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(mockInventory));

        // When / Then
        assertThrows(RuntimeException.class, () -> inventoryService.decrementInventoryOf(productId, amount));
        verify(inventoryRepository, times(1)).decrementStock(productId, amount);
        verify(inventoryRepository, times(1)).findByProductId(productId);
    }

    @Test
    void testCreateInventory_SavesInventorySuccessfully() {
        // Given
        InventoryEntity inventory = new InventoryEntity();
        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        // When
        InventoryEntity result = inventoryService.createInventory(inventory);

        // Then
        assertEquals(inventory, result);
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    void testUpdateInventory_UpdatesInventorySuccessfully() {
        // Given
        InventoryEntity inventory = new InventoryEntity();
        inventory.setInventoryId(UUID.randomUUID());
        when(inventoryRepository.findById(inventory.getInventoryId())).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        // When
        InventoryEntity result = inventoryService.updateInventory(inventory);

        // Then
        assertEquals(inventory, result);
        verify(inventoryRepository, times(1)).findById(inventory.getInventoryId());
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    void testUpdateInventory_ThrowsException_WhenInventoryNotExist() {
        // Given
        InventoryEntity inventory = new InventoryEntity();
        inventory.setInventoryId(UUID.randomUUID());
        when(inventoryRepository.findById(inventory.getInventoryId())).thenReturn(Optional.empty());

        // When / Then
        assertThrows(RuntimeException.class, () -> inventoryService.updateInventory(inventory));
        verify(inventoryRepository, times(1)).findById(inventory.getInventoryId());
    }

    @Test
    void testUpdateInventory_ThrowsException_WhenInventoryIdIsNull() {
        // Given
        InventoryEntity inventory = new InventoryEntity();

        // When / Then
        assertThrows(RuntimeException.class, () -> inventoryService.updateInventory(inventory));
        verify(inventoryRepository, never()).findById(any());
        verify(inventoryRepository, never()).save(inventory);
    }

    @Test
    void testDecrementInventoryOf_ThrowsException_WhenInventoryNotFound() {
        // Given
        UUID productId = UUID.randomUUID();
        Long amount = 5L;

        when(inventoryRepository.decrementStock(productId, amount)).thenReturn(0);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(RuntimeException.class, () -> inventoryService.decrementInventoryOf(productId, amount));
        verify(inventoryRepository, times(1)).decrementStock(productId, amount);
        verify(inventoryRepository, times(1)).findByProductId(productId);
    }
}