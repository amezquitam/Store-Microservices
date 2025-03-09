package com.example.productservice.repository;

import com.example.productservice.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends MongoRepository<Product, UUID> {

    /**
     * Buscar productos por categoría.
     *
     * @param category Categoría del producto.
     * @return Lista de productos pertenecientes a la categoría.
     */
    List<Product> findByCategory(String category);

    /**
     * Buscar productos por rango de precios.
     *
     * @param minPrice Precio mínimo.
     * @param maxPrice Precio máximo.
     * @return Lista de productos dentro del rango de precios especificado.
     */
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    /**
     * Buscar productos por nombre (case insensitive).
     *
     * @param name Nombre del producto.
     * @return Lista de productos cuyo nombre coincida (insensible a mayúsculas).
     */
    List<Product> findByNameIgnoreCase(String name);
}