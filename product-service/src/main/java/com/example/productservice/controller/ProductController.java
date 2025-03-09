package com.example.productservice.controller;

import com.example.productservice.entity.Product;
import com.example.productservice.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService; // Servicio no reactivo

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Crear un nuevo producto.
     * @param product Producto a crear.
     * @return Mono con el producto.
     */
    @PostMapping
    public Mono<ResponseEntity<Product>> createProduct(@RequestBody Product product) {
        return Mono.fromCallable(() -> ResponseEntity.ok(productService.createProduct(product)));
    }

    /**
     * Obtener un producto por su ID.
     * @param id ID del producto.
     * @return Mono con el producto encontrado o 404 si no existe.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Product>> getProductById(@PathVariable UUID id) {
        return Mono.fromCallable(() -> {
            Product product = productService.getProductById(id);
            return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
        });
    }

    /**
     * Obtener todos los productos.
     * @return Flux de productos.
     */
    @GetMapping
    public Flux<Product> getAllProducts() {
        return Flux.fromIterable(productService.getAllProducts());
    }

    /**
     * Obtiene productos por categoría.
     * @param category Categoría del producto.
     * @return Flux de productos que pertenecen a la categoría.
     */
    @GetMapping("/category/{category}")
    public Flux<Product> getProductsByCategory(@PathVariable String category) {
        return Flux.defer(() -> Flux.fromIterable(productService.getProductsByCategory(category)));
    }

    /**
     * Obtener productos por un rango de precios.
     * @param minPrice Precio mínimo.
     * @param maxPrice Precio máximo.
     * @return Flux de productos en el rango de precios.
     */
    @GetMapping("/price-range")
    public Flux<Product> getProductsByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        return Flux.defer(() -> Flux.fromIterable(productService.getProductsByPriceRange(minPrice, maxPrice)));
    }

    /**
     * Actualizar un producto existente por su ID.
     * @param id ID del producto.
     * @param updatedProduct Producto con datos a actualizar.
     * @return Mono con el producto actualizado o 404 si no existe.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Product>> updateProduct(
            @PathVariable UUID id,
            @RequestBody Product updatedProduct) {
        return Mono.fromCallable(() -> {
            Product updated = productService.updateProduct(id, updatedProduct);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        });
    }

    /**
     * Eliminar producto por su ID.
     * @param id ID del producto a eliminar.
     * @return Mono con respuesta vacía o 404 si no existe.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable UUID id) {
        return Mono.fromRunnable(() -> productService.deleteProduct(id))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }
}