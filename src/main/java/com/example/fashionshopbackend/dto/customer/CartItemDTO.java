package com.example.fashionshopbackend.dto.customer;

import com.example.fashionshopbackend.dto.product.ProductImageDTO;
import com.example.fashionshopbackend.dto.product.ProductVariantDTO;
import com.example.fashionshopbackend.dto.promotion.PromotedProductPromotionDTO;
import com.example.fashionshopbackend.dto.promotion.PromotionDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class CartItemDTO {
    private Long cartId;
    private Long userId;
    private Long productId;
    private String productName;
    private BigDecimal originalPrice;
    private ProductImageDTO primaryImage;
    private ProductVariantDTO variant;
    private Integer quantity;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private PromotedProductPromotionDTO promotion;
}
