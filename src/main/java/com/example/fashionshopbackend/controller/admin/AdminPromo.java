package com.example.fashionshopbackend.controller.admin;

import com.example.fashionshopbackend.dto.common.PagedResponse;
import com.example.fashionshopbackend.dto.promotion.PromotionDTO;
import com.example.fashionshopbackend.dto.promotion.UpdatePromotionDTO;
import com.example.fashionshopbackend.service.product.PromotionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/promotions")
public class AdminPromo {

    private static final Logger logger = LoggerFactory.getLogger(AdminPromo.class);

    @Autowired
    private PromotionService promotionService;

    // Lấy tất cả khuyến mãi cho admin
    @GetMapping("all")
    public ResponseEntity<PagedResponse<PromotionDTO>> getAllPromotionsForAdmin(Pageable pageable) {
        logger.info("Fetching all promotions for admin with pageable: {}", pageable);
        Page<PromotionDTO> promotions = promotionService.getPromotionsByCategories(pageable);
        PagedResponse<PromotionDTO> response = new PagedResponse<>(
                promotions.getContent(),
                promotions.getNumber(),
                promotions.getSize(),
                promotions.getTotalElements(),
                promotions.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }

    // Tạo khuyến mãi mới
    @PostMapping
    public ResponseEntity<PromotionDTO> createPromotion(@RequestBody UpdatePromotionDTO dto) {
        logger.info("Creating new promotion: {}", dto.getName());
        PromotionDTO createdPromotion = promotionService.createPromotion(dto);
        return ResponseEntity.ok(createdPromotion);
    }

    // Cập nhật khuyến mãi
    @PutMapping("/{id}")
    public ResponseEntity<PromotionDTO> updatePromotion(@PathVariable("id") Integer promotionId,
                                                        @RequestBody UpdatePromotionDTO dto) {
        logger.info("Updating promotion: {}", promotionId);
        PromotionDTO updatedPromotion = promotionService.updatePromotion(promotionId, dto);
        return ResponseEntity.ok(updatedPromotion);
    }

    // Xóa khuyến mãi
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Integer id) {
        logger.info("Deleting promotion: {}", id);
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }

    // Kích hoạt/hủy kích hoạt khuyến mãi
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<PromotionDTO> togglePromotionActive(@PathVariable Integer id) {
        logger.info("Toggling active status for promotion: {}", id);
        PromotionDTO updatedPromotion = promotionService.togglePromotionActive(id);
        return ResponseEntity.ok(updatedPromotion);
    }
}