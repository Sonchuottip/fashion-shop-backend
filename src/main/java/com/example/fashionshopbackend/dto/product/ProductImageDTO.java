package com.example.fashionshopbackend.dto.product;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ProductImageDTO {
    private Integer imageId;
    private String imageUrl;
    private Boolean isPrimary;
    private OffsetDateTime createdAt;
}