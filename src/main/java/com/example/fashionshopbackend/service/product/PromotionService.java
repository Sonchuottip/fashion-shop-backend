package com.example.fashionshopbackend.service.product;

import com.example.fashionshopbackend.dto.promotion.PromotionDTO;
import com.example.fashionshopbackend.dto.promotion.PromotedProductPromotionDTO;
import com.example.fashionshopbackend.entity.promotion.Promotion;
import com.example.fashionshopbackend.entity.product.Product;
import com.example.fashionshopbackend.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final long CACHE_TTL_SECONDS = 300; // 5 phút

    // Lấy danh sách khuyến mãi với danh mục
    public List<PromotionDTO> getPromotionsByCategories() {
        String cacheKey = "promotions:categories";
        List<PromotionDTO> cachedPromotions = (List<PromotionDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedPromotions != null) {
            return cachedPromotions;
        }

        List<Promotion> promotions = promotionRepository.findActivePromotions(LocalDate.now());
        List<PromotionDTO> dtos = promotions.stream().map(promotion -> {
            PromotionDTO dto = new PromotionDTO();
            dto.setPromotionId(promotion.getPromotionId());
            dto.setName(promotion.getName());
            dto.setDiscountPercent(promotion.getDiscountPercent());
            dto.setStartDate(promotion.getStartDate().toString());
            dto.setEndDate(promotion.getEndDate().toString());
            List<Integer> categoryIds = promotionRepository.findCategoryIdsByPromotionId(promotion.getPromotionId());
            dto.setCategoryIds(categoryIds);
            return dto;
        }).collect(Collectors.toList());

        redisTemplate.opsForValue().set(cacheKey, dtos, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        return dtos;
    }

    // Kiểm tra khuyến mãi áp dụng cho sản phẩm
    public Optional<Promotion> getApplicablePromotion(Product product) {
        Optional<Promotion> productPromotion = promotionRepository.findActivePromotionByProductId(
                product.getProductId(), LocalDate.now());
        Optional<Promotion> categoryPromotion = promotionRepository.findActivePromotionByCategoryId(
                product.getCategory().getCategoryId(), LocalDate.now());

        return productPromotion.isPresent() ? productPromotion : categoryPromotion;
    }

    // Tính giá giảm giá cho sản phẩm
    public BigDecimal calculateDiscountedPrice(BigDecimal originalPrice, Promotion promotion) {
        if (promotion == null) {
            return null;
        }
        BigDecimal discountPercent = promotion.getDiscountPercent();
        return originalPrice.multiply(BigDecimal.ONE.subtract(discountPercent.divide(BigDecimal.valueOf(100))));
    }

    // Tạo PromotedProductPromotionDTO từ Promotion
    public PromotedProductPromotionDTO createPromotionDTO(Promotion promotion) {
        if (promotion == null) {
            return null;
        }
        PromotedProductPromotionDTO dto = new PromotedProductPromotionDTO();
        dto.setPromotionId(promotion.getPromotionId());
        dto.setDiscountPercent(promotion.getDiscountPercent());
        return dto;
    }

    // Xóa cache khuyến mãi
    public void clearPromotionCache() {
        redisTemplate.delete(redisTemplate.keys("promotions:*"));
    }
}