package com.example.fashionshopbackend.entity.review;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "Reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReviewID")
    private Long reviewId;

    @Column(name = "UserID", nullable = false)
    private Long userId;

    @Column(name = "ProductID", nullable = false)
    private Long productId;

    @Column(name = "Rating", nullable = false)
    private Integer rating;

    @Column(name = "Comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "IsApproved")
    private Boolean isApproved = false;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private Instant createdAt;
}