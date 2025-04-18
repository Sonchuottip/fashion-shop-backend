package com.example.fashionshopbackend.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ProductImages")
@Data
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ImageID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ProductID", nullable = false)
    private Product product;

    @Column(name = "ImageURL", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "IsPrimary")
    private Boolean isPrimary = false;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}