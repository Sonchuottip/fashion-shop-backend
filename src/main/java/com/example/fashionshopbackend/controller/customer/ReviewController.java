package com.example.fashionshopbackend.controller.customer;

import com.example.fashionshopbackend.dto.admin.AdminResponse;
import com.example.fashionshopbackend.dto.customer.ReviewDTO;
import com.example.fashionshopbackend.service.review.ReviewService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/reviews")
@PreAuthorize("hasAuthority('Customer')")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewService reviewService;

    // [POST] /api/customer/reviews/add - Thêm đánh giá sản phẩm
    @PostMapping("/add")
    public ResponseEntity<?> addReview(@Valid @RequestBody ReviewDTO request) {
        try {
            logger.debug("Adding review for product ID: {}", request.getProductId());
            ReviewDTO reviewDTO = reviewService.addReview(request);
            logger.info("Added review for product ID: {}", request.getProductId());
            return ResponseEntity.ok(reviewDTO);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid review request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse("Lỗi: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to add review: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Lỗi khi thêm đánh giá: " + e.getMessage()));
        }
    }
}