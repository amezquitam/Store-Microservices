package com.example.productservice;

import com.example.productservice.entity.Product;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
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
    void givenProducts_whenFindByCategory_thenReturnProductsList() {
        // given
        Product product1 = new Product(UUID.randomUUID(), "Phone", "Electronics item", 799.99, "Electronics");
        Product product2 = new Product(UUID.randomUUID(), "Laptop", "Electronics item", 1299.99, "Electronics");
        productRepository.save(product1);
        productRepository.save(product2);

        // when
        List<Product> result = productRepository.findByCategory("Electronics");

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2).extracting(Product::getName).contains("Phone", "Laptop");
    }

    @Test
    void givenProducts_whenFindByCategory_thenReturnEmptyList() {
        // given
        Product product1 = new Product(UUID.randomUUID(), "Desk", "Wooden desk", 199.99, "Furniture");
        productRepository.save(product1);

        // when
        List<Product> result = productRepository.findByCategory("Electronics");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void givenProducts_whenFindByPriceBetween_thenReturnMatchingProducts() {
        // given
        Product product1 = new Product(UUID.randomUUID(), "Phone", "Mobile device", 700.00, "Electronics");
        Product product2 = new Product(UUID.randomUUID(), "Laptop", "Electronics item", 1200.00, "Electronics");
        Product product3 = new Product(UUID.randomUUID(), "Tablet", "Mobile device", 500.00, "Electronics");
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        // when
        List<Product> result = productRepository.findByPriceBetween(600.00, 1300.00);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2).extracting(Product::getName).contains("Phone", "Laptop");
    }

    @Test
    void givenProducts_whenFindByPriceBetween_thenReturnEmptyList() {
        // given
        Product product = new Product(UUID.randomUUID(), "Desk", "Wooden desk", 199.99, "Furniture");
        productRepository.save(product);

        // when
        List<Product> result = productRepository.findByPriceBetween(600.00, 1300.00);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void givenProducts_whenFindByNameIgnoreCase_thenReturnMatchingProduct() {
        // given
        Product product = new Product(UUID.randomUUID(), "Table", "Wooden furniture", 89.99, "Furniture");
        productRepository.save(product);

        // when
        List<Product> result = productRepository.findByNameIgnoreCase("table");

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1).extracting(Product::getName).contains("Table");
    }

    @Test
    void givenProduct_whenSave_thenProductIsSaved() {
        // given
        Product product = new Product(UUID.randomUUID(), "Chair", "Comfortable chair", 49.99, "Furniture");

        // when
        Product savedProduct = productRepository.save(product);

        // then
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Chair");
        assertThat(productRepository.findAll()).hasSize(1);
    }

    @Test
    void givenProductThatExists_whenDeleteById_thenProductIsDeleted() {
        // given
        Product product = new Product(UUID.randomUUID(), "Keyboard", "Mechanical keyboard", 79.99, "Electronics");
        product = productRepository.save(product);

        // when
        productRepository.deleteById(product.getId());

        // then
        assertThat(productRepository.findById(product.getId())).isEmpty();
        assertThat(productRepository.findAll()).isEmpty();
    }
}