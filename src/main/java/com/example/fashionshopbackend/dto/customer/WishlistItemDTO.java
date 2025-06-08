package com.example.fashionshopbackend.dto.customer;

import com.example.fashionshopbackend.dto.product.ProductImageDTO;
import com.example.fashionshopbackend.dto.promotion.PromotedProductPromotionDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class WishlistItemDTO {
    private Long wishlistId;
    private Long userId;
    private Integer productId;
    private String productName;
    private BigDecimal originalPrice;
    private BigDecimal discountedPrice;
    private ProductImageDTO primaryImage;
    private OffsetDateTime createdAt;
    private PromotedProductPromotionDTO promotion;
}
