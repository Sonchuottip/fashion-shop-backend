package com.example.fashionshopbackend.dto.product;

import lombok.Data;

@Data
public class ProductDTO {
    private Integer productId;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private Integer categoryId;
    private String status;
    private String imageUrl; // Thêm trường cho URL của ảnh chính
}