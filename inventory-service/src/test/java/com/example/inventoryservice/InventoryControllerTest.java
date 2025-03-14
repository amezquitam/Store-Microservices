package com.example.inventoryservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@WebFluxTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    void testGetAllInventories_ReturnsAllInventories() {
        // Given
        List<InventoryEntity> inventories = List.of(new InventoryEntity(), new InventoryEntity());
        when(inventoryService.getAllInventories()).thenReturn(inventories);

        // When / Then
        webTestClient.get()
                .uri("/api/inventories")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryEntity.class)
                .isEqualTo(inventories);

        verify(inventoryService, times(1)).getAllInventories();
    }

    @Test
    void testGetInventoryByProductId_ReturnsInventory() {
        // Given
        UUID productId = UUID.randomUUID();
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(productId);

        when(inventoryService.getInventoryOf(productId)).thenReturn(inventory);

        // When / Then
        webTestClient.get()
                .uri("/api/inventories/" + productId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(InventoryEntity.class)
                .isEqualTo(inventory);

        verify(inventoryService, times(1)).getInventoryOf(productId);
    }

    @Test
    void testGetInventoryByProductId_ReturnsNotFound() {
        // Given
        UUID productId = UUID.randomUUID();
        when(inventoryService.getInventoryOf(productId)).thenThrow(new NoSuchElementException("Inventory not found"));

        // When / Then
        webTestClient.get()
                .uri("/api/inventories/" + productId)
                .exchange()
                .expectStatus().isNotFound();

        verify(inventoryService, times(1)).getInventoryOf(productId);
    }

    @Test
    void testCreateInventory_CreatesInventory() {
        // Given
        InventoryEntity inventory = new InventoryEntity();
        inventory.setStock(100L);
        when(inventoryService.createInventory(any(InventoryEntity.class))).thenReturn(inventory);

        // When / Then
        webTestClient.post()
                .uri("/api/inventories")
                .bodyValue(inventory)
                .exchange()
                .expectStatus().isOk()
                .expectBody(InventoryEntity.class)
                .isEqualTo(inventory);

        verify(inventoryService, times(1)).createInventory(any(InventoryEntity.class));
    }

    @Test
    void testUpdateInventory_UpdatesInventorySuccessfully() {
        // Given
        UUID inventoryId = UUID.randomUUID();
        InventoryEntity updatedInventory = new InventoryEntity();
        updatedInventory.setInventoryId(inventoryId);
        updatedInventory.setStock(50L);

        when(inventoryService.updateInventory(any(InventoryEntity.class))).thenReturn(updatedInventory);

        // When / Then
        webTestClient.put()
                .uri("/api/inventories/" + inventoryId)
                .bodyValue(updatedInventory)
                .exchange()
                .expectStatus().isOk()
                .expectBody(InventoryEntity.class)
                .isEqualTo(updatedInventory);

        verify(inventoryService, times(1)).updateInventory(any(InventoryEntity.class));
    }

    @Test
    void testUpdateInventory_ReturnsNotFound() {
        // Given
        UUID inventoryId = UUID.randomUUID();
        InventoryEntity inventoryToUpdate = new InventoryEntity();
        inventoryToUpdate.setInventoryId(inventoryId);

        when(inventoryService.updateInventory(any(InventoryEntity.class))).thenThrow(new RuntimeException("Inventory not found"));

        // When / Then
        webTestClient.put()
                .uri("/api/inventories/" + inventoryId)
                .bodyValue(inventoryToUpdate)
                .exchange()
                .expectStatus().isNotFound();

        verify(inventoryService, times(1)).updateInventory(any(InventoryEntity.class));
    }

    @Test
    void testDecrementStock_DecrementsStockSuccessfully() {
        // Given
        UUID productId = UUID.randomUUID();

        when(inventoryService.decrementInventoryOf(productId, 10L)).thenReturn(Boolean.TRUE);

        // When / Then
        webTestClient.patch()
                .uri("/api/inventories/" + productId + "/decrement?amount=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Stock decremented successfully");

        verify(inventoryService, times(1)).decrementInventoryOf(productId, 10L);
    }

    @Test
    void testDecrementStock_ReturnsError_WhenInsufficientStock() {
        // Given
        UUID productId = UUID.randomUUID();
        when(inventoryService.decrementInventoryOf(productId, 10L))
                .thenThrow(new RuntimeException("Insufficient stock"));

        // When / Then
        webTestClient.patch()
                .uri("/api/inventories/" + productId + "/decrement?amount=10")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Insufficient stock");

        verify(inventoryService, times(1)).decrementInventoryOf(productId, 10L);
    }

    @Test
    void testGetAllInventories_ReturnsEmptyList() {
        // Given
        when(inventoryService.getAllInventories()).thenReturn(List.of());

        // When / Then
        webTestClient.get()
                .uri("/api/inventories")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryEntity.class)
                .hasSize(0);

        verify(inventoryService, times(1)).getAllInventories();
    }

    @Test
    void testCreateInventory_ReturnsError_ForInvalidData() {
        // Given
        InventoryEntity invalidInventory = new InventoryEntity();
        invalidInventory.setStock(-10L);

        when(inventoryService.createInventory(any(InventoryEntity.class)))
                .thenThrow(new RuntimeException("Invalid stock value"));

        // When / Then
        webTestClient.post()
                .uri("/api/inventories")
                .bodyValue(invalidInventory)
                .exchange()
                .expectStatus().isBadRequest();

        verify(inventoryService, times(1)).createInventory(any(InventoryEntity.class));
    }


}