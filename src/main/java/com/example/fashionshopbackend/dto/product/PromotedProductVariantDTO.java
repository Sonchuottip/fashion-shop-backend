package com.example.fashionshopbackend.dto.product;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PromotedProductVariantDTO {
    private Integer variantId;
    private String sku;
    private String color;
    private String size;
    private BigDecimal originalPrice;
    private BigDecimal discountedPrice;
    private Integer stock;
}