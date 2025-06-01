package com.example.fashionshopbackend.dto.promotion;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PromotedProductPromotionDTO {
    private Integer promotionId;
    private BigDecimal discountPercent;
}