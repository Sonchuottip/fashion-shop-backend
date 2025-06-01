package com.example.fashionshopbackend.dto.product;

import com.example.fashionshopbackend.dto.promotion.PromotedProductPromotionDTO;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PromotedProductDTO {
    private Integer productId;
    private String name;
    private String description;
    private BigDecimal originalPrice;
    private BigDecimal discountedPrice;
    private Integer categoryId;
    private String gender;
    private BigDecimal averageRating;
    private Integer totalSold;
    private Integer totalLikes;
    private PromotedProductPromotionDTO promotion;
    private List<PromotedProductVariantDTO> variants;
}