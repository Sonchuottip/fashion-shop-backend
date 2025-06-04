package com.example.fashionshopbackend.dto.customer;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ReviewDTO {
    private Long reviewId;
    private String fullName;
    private String avatarUrl;
    private Integer rating;
    private String comment;
    private OffsetDateTime createdAt;
    private String productName;
}
