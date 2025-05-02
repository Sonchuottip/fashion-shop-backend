package com.example.fashionshopbackend.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductVariantDTO {
    private Integer variantId; // Dùng cho cập nhật, có thể null khi tạo mới
    private Integer productId; // Liên kết với sản phẩm
    private String sku;
    private String color;
    private String size;
    private Double price;
    private Integer stock;
}