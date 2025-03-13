package com.example.productservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Document(collection = "products")
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private UUID id = UUID.randomUUID();

    private String name;

    private String description;

    private Double price;

    private String category;
}