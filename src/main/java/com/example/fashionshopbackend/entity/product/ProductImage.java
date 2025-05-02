package com.example.fashionshopbackend.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "ProductImages")
@Data
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ImageID")
    private Integer imageId;

    @Column(name = "ProductID", nullable = false)
    private Integer productId;

    @Column(name = "ImageURL", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "IsPrimary")
    private Boolean isPrimary;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private Instant createdAt;
}