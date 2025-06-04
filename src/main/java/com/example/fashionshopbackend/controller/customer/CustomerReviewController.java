package com.example.fashionshopbackend.controller.customer;

import com.example.fashionshopbackend.dto.common.PagedResponse;
import com.example.fashionshopbackend.dto.review.ReportRequest;
import com.example.fashionshopbackend.dto.review.ReviewDTO;
import com.example.fashionshopbackend.dto.review.ReviewRequest;
import com.example.fashionshopbackend.entity.review.Review;
import com.example.fashionshopbackend.service.customer.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
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

    @GetMapping("/product/{productId}")
    public ResponseEntity<PagedResponse<ReviewDTO>> getReviewsByProductId(
            @PathVariable Integer productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<ReviewDTO> reviews = reviewService.getReviewsByProductId(productId, rating, page, size);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PagedResponse<ReviewDTO>> getReviewsByUserId(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<ReviewDTO> reviews = reviewService.getReviewsByUserId(userId, page, size);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/report")
    public ResponseEntity<String> reportReview(@RequestBody ReportRequest request) {
        try {
            reviewService.reportReview(request.getUserId(), request.getReviewId(), request.getReason());
            return ResponseEntity.ok("Báo cáo đánh giá thành công");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Đánh giá không tồn tại");
        }
    }

    @GetMapping("/reported")
    public ResponseEntity<PagedResponse<ReviewDTO>> getReportedReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<ReviewDTO> reviews = reviewService.getReportedReviews(page, size);
        return ResponseEntity.ok(reviews);
    }

    @PatchMapping("/{reviewId}/approve")
    public ResponseEntity<String> approveReview(@PathVariable Long reviewId) {
        try {
            reviewService.approveReview(reviewId);
            return ResponseEntity.ok("Phê duyệt đánh giá thành công");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Đánh giá không tồn tại");
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok("Xóa đánh giá thành công");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Đánh giá không tồn tại");
        }
    }
}


