package com.example.fashionshopbackend.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductVariantDTO {
    @NotNull
    private Integer productId;

    @NotBlank
    private String sku;

    private String color;

    private String size;

    @NotNull
    @Positive
    private Double price;

    @NotNull
    @Positive
    private Integer stock;
}