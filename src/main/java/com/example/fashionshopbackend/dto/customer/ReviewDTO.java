package com.example.fashionshopbackend.dto.customer;

import lombok.Data;

@Data
public class ReviewDTO {
    private Long reviewId;
    private Long userId;
    private Long productId;
    private Integer rating;
    private String comment;
    private Boolean isApproved;
    private String createdAt;
}