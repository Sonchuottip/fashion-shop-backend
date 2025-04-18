package com.example.fashionshopbackend.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class ProductWithImagesDTO {
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

    private List<ProductImageDTO> images;
}