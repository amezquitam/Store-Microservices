package com.example.productservice.service;

import com.example.productservice.entity.Product;
import com.example.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    // Constructor para inyección del repositorio
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product createProduct(Product product) {
        // Guardar producto usando el repositorio de MongoDB
        return productRepository.save(product);
    }

    @Override
    public Product getProductById(UUID id) {
        // Buscar producto por ID y manejar si no se encuentra
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    @Override
    public List<Product> getAllProducts() {
        // Recuperar todos los productos
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        // Filtrar productos por categoría
        return productRepository.findByCategory(category);
    }

    @Override
    public List<Product> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        // Productos dentro del rango de precios
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Override
    public Product updateProduct(UUID id, Product updatedProduct) {
        // Encontrar producto existente, actualizar y guardar
        Product existingProduct = getProductById(id);
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setCategory(updatedProduct.getCategory());
        return productRepository.save(existingProduct);
    }

    @Override
    public UUID deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        productRepository.deleteById(id);
        return id;
    }
}