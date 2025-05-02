package com.example.fashionshopbackend.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class ProductWithImagesAndVariantsDTO {
    private Integer productId; // Dùng cho cập nhật, có thể null khi tạo mới
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private Integer categoryId;
    private String status;
    private List<ProductImageDTO> images;
    private List<ProductVariantDTO> variants;
}