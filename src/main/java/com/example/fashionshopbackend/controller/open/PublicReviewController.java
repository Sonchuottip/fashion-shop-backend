package com.example.fashionshopbackend.controller.open;

import com.example.fashionshopbackend.dto.customer.ReviewDTO;
import com.example.fashionshopbackend.service.review.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class PublicReviewController {

    private static final Logger logger = LoggerFactory.getLogger(PublicReviewController.class);

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByProduct(@PathVariable Long productId) {
        try {
            List<ReviewDTO> reviews = reviewService.getApprovedReviewsByProduct(productId);
            logger.info("Retrieved reviews for product {}", productId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            logger.error("Failed to retrieve reviews: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}