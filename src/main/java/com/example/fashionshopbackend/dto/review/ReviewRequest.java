package com.example.fashionshopbackend.dto.review;

import lombok.Data;

@Data
public class ReviewRequest {
    private Integer userId;
    private Integer productId;
    private Integer rating;
    private String comment;
}
