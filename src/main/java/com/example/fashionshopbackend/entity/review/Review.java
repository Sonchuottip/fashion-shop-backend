package com.example.fashionshopbackend.entity.store;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Data
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        isApproved = false;
    }
}