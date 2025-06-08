package com.example.fashionshopbackend.controller.admin;

import com.example.fashionshopbackend.dto.common.PagedResponse;
import com.example.fashionshopbackend.dto.review.ReviewDTO;
import com.example.fashionshopbackend.service.customer.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

    @Autowired
    private ReviewService reviewService;

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
            reviewService.deleteReviewByAdmin(reviewId);
            return ResponseEntity.ok("Xóa đánh giá thành công");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Đánh giá không tồn tại");
        }
    }
}