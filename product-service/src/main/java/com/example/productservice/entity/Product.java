package com.example.productservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

import java.util.UUID;

@Data // Lombok para generar getters, setters, etc.
@Document(collection = "products") // Define que esta clase corresponde a la colección 'products' en MongoDB
public class Product {

    @Id
    private UUID id;

    private String name;

    private String description;

    private Double price;

    private Integer stock;

    private String category;

    private String imageUrl;
}