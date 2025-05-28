package com.example.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {
    @GetMapping("/fallback/products")
    public ResponseEntity<String> productFallback() {
        return ResponseEntity.ok("Servicio de productos no disponible temporalmente.");
    }

    @GetMapping("/fallback/orders")
    public ResponseEntity<String> orderFallback() {
        return ResponseEntity.ok("Servicio de ordenes no disponible temporalmente.");
    }

    @GetMapping("/fallback/inventories")
    public ResponseEntity<String> inventoryFallback() {
        return ResponseEntity.ok("Servicio de inventario no disponible temporalmente.");
    }

    @GetMapping("/fallback/payments")
    public ResponseEntity<String> paymentFallback() {
        return ResponseEntity.ok("Servicio de pagos no disponible temporalmente.");
    }
}
