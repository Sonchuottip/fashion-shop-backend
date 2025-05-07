package com.example.fashionshopbackend.controller.open;

import com.example.fashionshopbackend.dto.review.ReviewDTO;
import com.example.fashionshopbackend.service.review.ReviewService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/add")
    public ResponseEntity<ReviewDTO> addReview(@Valid @RequestBody ReviewDTO request) {
        try {
            ReviewDTO reviewDTO = reviewService.addReview(request);
            logger.info("Added review for product {}", request.getProductId());
            return ResponseEntity.ok(reviewDTO);
        } catch (Exception e) {
            logger.error("Failed to add review: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

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