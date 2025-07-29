package com.myshop.productservice.repository;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    private UUID id;

    private String name;

    private String description;

    private double price;

    private String article;

    private int quantity;

    private boolean active;

    private LocalDateTime createdAt;

}
