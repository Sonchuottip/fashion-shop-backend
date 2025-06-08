package com.example.fashionshopbackend.controller.open;

import com.example.fashionshopbackend.dto.common.PagedResponse;
import com.example.fashionshopbackend.dto.review.ReviewDTO;
import com.example.fashionshopbackend.service.customer.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<PagedResponse<ReviewDTO>> getReviewsByProductId(
            @PathVariable Integer productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<ReviewDTO> reviews = reviewService.getReviewsByProductId(productId, rating, page, size);
        return ResponseEntity.ok(reviews);
    }
}