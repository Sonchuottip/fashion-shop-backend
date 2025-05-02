package com.example.fashionshopbackend.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "Products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductID")
    private Integer productId;

    @Column(name = "Name", nullable = false, length = 255)
    private String name;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "Price", nullable = false)
    private Double price;

    @Column(name = "Stock")
    private Integer stock;

    @Column(name = "CategoryID")
    private Integer categoryId;

    @Column(name = "Status", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'Active'")
    private String status;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "UpdatedAt", insertable = false)
    private Instant updatedAt;
}