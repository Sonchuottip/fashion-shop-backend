package com.example.fashionshopbackend.dto.promotion;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PromotionDTO {
    private Integer promotionId;
    private String name;
    private BigDecimal discountPercent;
    private String startDate;
    private String endDate;
    private List<Integer> categoryIds; // Danh sách categoryId liên kết
}