package com.example.fashionshopbackend.dto.product;

import com.example.fashionshopbackend.dto.promotion.PromotedProductPromotionDTO;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class ProductDTO {
    private Integer productId;
    private String name;
    private String description;
    private BigDecimal originalPrice; // Giá gốc
    private BigDecimal discountedPrice; // Giá sau giảm (null nếu không có khuyến mãi)
    private Integer categoryId;
    private String gender;
    private BigDecimal averageRating;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private ProductImageDTO primaryImage;
    private Integer totalSold;
    private Integer totalLikes;
    private PromotedProductPromotionDTO promotion; // Chỉ chứa promotionId và discountPercent (null nếu không có)
    private List<ProductVariantDTO> variants;
    private List<ProductImageDTO> images; // Thêm để hỗ trợ chi tiết sản phẩm
}