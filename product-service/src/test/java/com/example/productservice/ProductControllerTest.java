package com.example.productservice;

import com.example.productservice.controller.ProductController;
import com.example.productservice.entity.Product;
import com.example.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@WebFluxTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private ProductService productService;
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testCreatePayment() {
        Product product = new Product();
        product.setName("test");
        product.setDescription("description");

        when(productService.createProduct(product)).thenReturn(product);
        webTestClient.post()
                .uri("/api/products")
                .bodyValue(product)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .isEqualTo(product);

        verify(productService, times(1)).createProduct(product);
    }

    @Test
    void testGetProducts(){
        List<Product> products = new ArrayList<>();
        products.add(new Product());
        products.add(new Product());

        when(productService.getAllProducts()).thenReturn(products);
        webTestClient.get()
                .uri("/api/products")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class)
                .isEqualTo(products);
        verify(productService, times(1)).getAllProducts();

    }
}
