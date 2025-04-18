package com.example.fashionshopbackend.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class ProductWithImagesAndVariantsDTO {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Positive
    private Double price;

    @NotNull
    @Positive
    private Integer stock;

    @Pattern(regexp = "Active|Inactive|Deleted", message = "Status must be 'Active', 'Inactive', or 'Deleted'")
    private String status;

    private Integer categoryId;

    private List<ProductImageDTO> images; // Danh sách ảnh

    private List<ProductVariantDTO> variants; // Danh sách biến thể
}