package com.example.fashionshopbackend.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "ProductVariants")
@Data
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VariantID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ProductID", nullable = false)
    private Product product;

    @Column(name = "SKU", nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "Color", length = 50)
    private String color;

    @Column(name = "Size", length = 10)
    private String size;

    @Column(name = "Price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Thay Double bằng BigDecimal

    @Column(name = "Stock", nullable = false)
    private Integer stock = 0;
}