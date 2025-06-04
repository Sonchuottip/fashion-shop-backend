package com.example.fashionshopbackend.dto.customer;

import lombok.Data;

@Data
public class ReviewRequest {
    private Integer userId;
    private Integer productId;
    private Integer rating;
    private String comment;
}
