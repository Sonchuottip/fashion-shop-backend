package com.example.fashionshopbackend.dto.promotion;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PromotionDetailsDTO {
    private PromotionDTO promotion;
    private List<ProductDTO> products;
    private List<CategoryDTO> categories;

    @Data
    public static class ProductDTO {
        private Integer productId;
        private String name;
        private BigDecimal originalPrice;
        private BigDecimal discountedPrice;
    }

    @Data
    public static class CategoryDTO {
        private Integer categoryId;
        private String name;
    }
}