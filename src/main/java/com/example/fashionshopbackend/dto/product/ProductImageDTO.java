package com.example.fashionshopbackend.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductImageDTO {
    @NotNull
    private Integer productId;

    @NotBlank
    private String imageUrl;

    private Boolean isPrimary;
}