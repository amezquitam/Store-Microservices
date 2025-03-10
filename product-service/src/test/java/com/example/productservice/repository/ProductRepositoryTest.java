package com.example.productservice.repository;

import com.example.productservice.AbstractDataMongoTest;
import com.example.productservice.entity.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRepositoryTest extends AbstractDataMongoTest {
    @Autowired
    ProductRepository productRepository;

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    void givenAProduct_whenSave_idReturnedIsNotNull() {
        Product product = new Product(UUID.randomUUID(), "Laptop", "Electronics", 999.99, "Electronics");
        Product savedProduct = productRepository.save(product);

        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Laptop");

    }

}