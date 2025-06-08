package com.example.fashionshopbackend.controller.customer;

import com.example.fashionshopbackend.dto.common.PagedResponse;
import com.example.fashionshopbackend.dto.review.ReportRequest;
import com.example.fashionshopbackend.dto.review.ReviewDTO;
import com.example.fashionshopbackend.dto.review.ReviewRequest;
import com.example.fashionshopbackend.entity.review.Review;
import com.example.fashionshopbackend.service.customer.ReviewService;
import com.example.fashionshopbackend.service.common.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/reviews")
    public ResponseEntity<Review> addReview(@RequestBody ReviewRequest request) {
        try {
            Review review = reviewService.addReview(
                    request.getUserId(),
                    request.getProductId(),
                    request.getRating(),
                    request.getComment()
            );
            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/reviews/user/{userId}")
    public ResponseEntity<PagedResponse<ReviewDTO>> getReviewsByUserId(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<ReviewDTO> reviews = reviewService.getReviewsByUserId(userId, page, size);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/reviews/report")
    public ResponseEntity<String> reportReview(@RequestBody ReportRequest request) {
        try {
            reviewService.reportReview(request.getUserId(), request.getReviewId(), request.getReason());
            return ResponseEntity.ok("Báo cáo đánh giá thành công");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Đánh giá không tồn tại");
        }
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<Review> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewRequest request) {
        try {
            Review review = reviewService.updateReview(
                    reviewId,
                    request.getUserId(),
                    request.getRating(),
                    request.getComment()
            );
            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long reviewId,
            @RequestParam Integer userId) {
        try {
            reviewService.deleteReviewByCustomer(reviewId, userId);
            return ResponseEntity.ok("Xóa đánh giá thành công");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}