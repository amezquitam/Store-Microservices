package com.example.productservice.service;

import com.example.productservice.entity.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    /**
     * Crear un nuevo producto.
     *
     * @param product Producto a crear.
     * @return El producto creado con su ID generado.
     */
    Product createProduct(Product product);

    /**
     * Obtener un producto por su ID.
     *
     * @param id ID del producto.
     * @return El producto encontrado.
     */
    Product getProductById(UUID id);

    /**
     * Obtener todos los productos.
     *
     * @return Lista de todos los productos.
     */
    List<Product> getAllProducts();

    /**
     * Obtener productos por categoría.
     *
     * @param category Categoría del producto.
     * @return Lista de productos en la categoría especificada.
     */
    List<Product> getProductsByCategory(String category);

    /**
     * Obtener productos dentro de un rango de precios.
     *
     * @param minPrice Precio mínimo.
     * @param maxPrice Precio máximo.
     * @return Lista de productos en el rango de precios.
     */
    List<Product> getProductsByPriceRange(Double minPrice, Double maxPrice);

    /**
     * Actualizar un producto existente.
     *
     * @param id ID del producto a actualizar.
     * @param updatedProduct Objeto con los datos actualizados.
     * @return El producto actualizado.
     */
    Product updateProduct(UUID id, Product updatedProduct);

    /**
     * Eliminar un producto por su ID.
     *
     * @param id ID del producto a eliminar.
     */
    void deleteProduct(UUID id);
}