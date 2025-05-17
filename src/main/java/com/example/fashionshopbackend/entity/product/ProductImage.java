package com.example.fashionshopbackend.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.time.OffsetDateTime;

@Entity
@Table(name = "Product_Images")
@Data
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Image_ID")
    private Integer imageId;

    @Column(name = "Product_ID", nullable = false)
    private Integer productId;

    @Column(name = "Image_URL", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "Is_Primary")
    private Boolean isPrimary;

    @Column(name = "Created_At", insertable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}