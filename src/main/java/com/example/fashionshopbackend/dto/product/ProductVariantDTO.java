package com.example.fashionshopbackend.dto.product;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductVariantDTO {
    private Integer variantId;
    private String sku;
    private String color;
    private String size;
    private BigDecimal originalPrice; // Giá gốc
    private BigDecimal discountedPrice; // Giá sau giảm (null nếu không có khuyến mãi)
    private String status;
    private Integer stock;
}