package com.example.inventoryservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // GET: Fetch all inventories
    @GetMapping
    public Flux<InventoryEntity> getAllInventories() {
        return Flux.defer(() -> Flux.fromIterable(inventoryService.getAllInventories()));
    }

    // GET: Fetch a specific inventory by product ID
    @GetMapping("/{productId}")
    public Mono<ResponseEntity<InventoryEntity>> getInventoryByProductId(@PathVariable UUID productId) {
        return Mono.defer(() ->
                Mono.just(inventoryService.getInventoryOf(productId))
                        .map(ResponseEntity::ok)
                        .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()))
        );
    }

    // POST: Create a new inventory
    @PostMapping
    public Mono<InventoryEntity> createInventory(@RequestBody InventoryEntity inventoryEntity) {
        return Mono.defer(() -> Mono.just(inventoryService.createInventory(inventoryEntity)));
    }

    // PUT: Update an existing inventory
    @PutMapping("/{inventoryId}")
    public Mono<ResponseEntity<InventoryEntity>> updateInventory(
            @PathVariable UUID inventoryId,
            @RequestBody InventoryEntity inventoryEntity
    ) {
        return Mono.defer(() -> {
            inventoryEntity.setInventoryId(inventoryId);
            try {
                return Mono.just(inventoryService.updateInventory(inventoryEntity))
                        .map(ResponseEntity::ok);
            } catch (RuntimeException e) {
                return Mono.just(ResponseEntity.notFound().build());
            }
        });
    }

    // PATCH: Decrement the stock for a product
    @PatchMapping("/{productId}/decrement")
    public Mono<ResponseEntity<String>> decrementStock(
            @PathVariable UUID productId,
            @RequestParam Long amount
    ) {
        return Mono.defer(() -> {
            try {
                inventoryService.decrementInventoryOf(productId, amount);
                return Mono.just(ResponseEntity.ok("Stock decremented successfully"));
            } catch (RuntimeException e) {
                return Mono.just(ResponseEntity.badRequest().body(e.getMessage()));
            }
        });
    }
}