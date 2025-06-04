package com.example.fashionshopbackend.dto.promotion;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdatePromotionDTO {
    private String name;
    private String description;
    private BigDecimal discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private String appliesTo;
    private String gender;
    private boolean isActive;
    private List<Integer> categoryIds;
    private List<Integer> productIds;
}