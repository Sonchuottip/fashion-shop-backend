package com.example.fashionshopbackend.controller.open;

import com.example.fashionshopbackend.dto.promotion.PromotionDTO;
import com.example.fashionshopbackend.dto.promotion.PromotedProductPromotionDTO;
import com.example.fashionshopbackend.dto.promotion.PromotionDetailsDTO;
import com.example.fashionshopbackend.service.product.PromotionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PromotionController {

    private static final Logger logger = LoggerFactory.getLogger(PromotionController.class);

    @Autowired
    private PromotionService promotionService;

    // Lấy tất cả khuyến mãi (phân trang)
    @GetMapping("/promotions")
    public ResponseEntity<Page<PromotionDTO>> getAllPromotions(Pageable pageable) {
        logger.info("Fetching all promotions with pageable: {}", pageable);
        Page<PromotionDTO> promotions = promotionService.getPromotionsByCategories(pageable);
        return ResponseEntity.ok(promotions);
    }

    // Lấy danh sách khuyến mãi với danh mục (phân trang)
    @GetMapping("/promotions/categories")
    public ResponseEntity<Page<PromotionDTO>> getPromotionsByCategories(Pageable pageable) {
        logger.info("Fetching promotions by categories with pageable: {}", pageable);
        Page<PromotionDTO> promotions = promotionService.getPromotionsByCategories(pageable);
        return ResponseEntity.ok(promotions);
    }

    // Lấy khuyến mãi áp dụng cho sản phẩm/danh mục
    @GetMapping("/promotions/applicable")
    public ResponseEntity<PromotedProductPromotionDTO> getApplicablePromotion(
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) Integer categoryId) {
        logger.info("Fetching applicable promotion for productId: {}, categoryId: {}", productId, categoryId);
        PromotedProductPromotionDTO promotion = promotionService.getApplicablePromotionDTO(productId, categoryId);
        return ResponseEntity.ok(promotion);
    }

    // Lấy chi tiết khuyến mãi áp dụng
    @GetMapping("/promotions/applicable/details")
    public ResponseEntity<PromotionDetailsDTO> getPromotionDetails(
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) Integer categoryId) {
        logger.info("Fetching promotion details for productId: {}, categoryId: {}", productId, categoryId);
        PromotionDetailsDTO details = promotionService.getPromotionDetails(productId, categoryId);
        return ResponseEntity.ok(details);
    }
}