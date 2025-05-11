package com.example.fashionshopbackend.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "Product_Variants")
@Data
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VariantID")
    private Integer variantId;

    @Column(name = "ProductID", nullable = false)
    private Integer productId;

    @Column(name = "SKU", unique = true, nullable = false, length = 50)
    private String sku;

    @Column(name = "Color", length = 50)
    private String color;

    @Column(name = "Size", length = 10)
    private String size;

    @Column(name = "Price", nullable = false)
    private Double price;

    @Column(name = "Stock")
    private Integer stock;

    @Column(name = "Created_At", insertable = false, updatable = false)
    private Instant createdAt;
}