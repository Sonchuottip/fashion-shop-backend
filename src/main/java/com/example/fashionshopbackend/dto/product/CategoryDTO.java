package com.example.fashionshopbackend.dto.product;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CategoryDTO{
    private Integer categoryId;
    private String name;
    private String description;
    private Integer parentCategoryId;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}