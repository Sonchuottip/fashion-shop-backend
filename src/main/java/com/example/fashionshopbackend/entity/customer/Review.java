package com.example.fashionshopbackend.entity.customer;

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
    @Column(name = "Review_ID")
    private Long reviewId;

    @Column(name = "User_ID", nullable = false)
    private Long userId;

    @Column(name = "Product_ID", nullable = false)
    private Long productId;

    @Column(name = "Rating", nullable = false)
    private Integer rating;

    @Column(name = "Comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "Is_Approved")
    private Boolean isApproved = false;

    @Column(name = "Created_At", insertable = false, updatable = false)
    private Instant createdAt;
}