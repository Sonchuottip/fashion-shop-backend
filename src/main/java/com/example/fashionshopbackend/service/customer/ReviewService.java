package com.example.fashionshopbackend.service.customer;

import com.example.fashionshopbackend.dto.common.PagedResponse;
import com.example.fashionshopbackend.dto.review.ReviewDTO;
import com.example.fashionshopbackend.entity.review.*;
import com.example.fashionshopbackend.repository.ProductRepository;
import com.example.fashionshopbackend.repository.ReviewReportRepository;
import com.example.fashionshopbackend.repository.ReviewRepository;
import com.example.fashionshopbackend.repository.UserProfileRepository;
import com.example.fashionshopbackend.service.common.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewReportRepository reviewReportRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Review addReview(Integer userId, Integer productId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Điểm đánh giá phải từ 1 đến 5");
        }

        Optional<Review> existingReview = reviewRepository.findByUserIdAndProductId(userId, productId);
        if (existingReview.isPresent()) {
            throw new IllegalArgumentException("Bạn đã đánh giá sản phẩm này rồi");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(productId);
        review.setRating(rating);
        review.setComment(comment);
        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReviewDTO> getReviewsByProductId(Integer productId, Integer rating, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage;
        if (rating != null && rating >= 1 && rating <= 5) {
            reviewPage = reviewRepository.findByProductIdAndRatingAndIsApprovedTrue(productId, rating, pageable);
        } else {
            reviewPage = reviewRepository.findByProductIdAndIsApprovedTrue(productId, pageable);
        }

        List<ReviewDTO> reviewDTOs = reviewPage.getContent().stream()
                .map(this::convertToReviewDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                reviewDTOs,
                page,
                size,
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReviewDTO> getReviewsByUserId(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findByUserId(userId, pageable);

        List<ReviewDTO> reviewDTOs = reviewPage.getContent().stream()
                .map(this::convertToReviewDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                reviewDTOs,
                page,
                size,
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReviewDTO> getReportedReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findByIsApprovedFalse(pageable);

        List<ReviewDTO> reviewDTOs = reviewPage.getContent().stream()
                .map(this::convertToReviewDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                reviewDTOs,
                page,
                size,
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages()
        );
    }

    @Transactional
    public void reportReview(Integer userId, Long reviewId, String reason) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Đánh giá không tồn tại"));

        review.setIsApproved(false);
        reviewRepository.save(review);

        ReviewReport report = new ReviewReport();
        report.setReviewId(reviewId);
        report.setUserId(userId);
        report.setReason(reason);
        reviewReportRepository.save(report);

        // Gửi thông báo cho admin
        notificationService.createAdminNotification(
                1, // Giả sử adminId=1, thay bằng logic lấy adminId thực tế
                "REVIEW_REPORTED",
                reviewId,
                String.format("Đánh giá #%d đã bị báo cáo với lý do: %s", reviewId, reason),
                null,
                3, // Ưu tiên trung bình
                userId, // Người gửi là user báo cáo
                null
        );
    }

    @Transactional
    public void approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Đánh giá không tồn tại"));
        review.setIsApproved(true);
        reviewRepository.save(review);

        // Gửi thông báo cho customer
        notificationService.createCustomerNotification(
                review.getUserId(),
                "REVIEW_APPROVED",
                reviewId,
                String.format("Đánh giá của bạn cho sản phẩm #%d đã được phê duyệt", review.getProductId()),
                null,
                2, // Ưu tiên thấp
                null, // Hệ thống gửi
                null
        );
    }

    @Transactional
    public void deleteReviewByCustomer(Long reviewId, Integer userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Đánh giá không tồn tại"));

        if (!review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền xóa đánh giá này");
        }

        reviewRepository.deleteById(reviewId);
        logger.info("Customer userId: {} đã xóa đánh giá reviewId: {}", userId, reviewId);
    }

    @Transactional
    public void deleteReviewByAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Đánh giá không tồn tại"));

        Integer userId = review.getUserId();
        Integer productId = review.getProductId();

        // Gửi thông báo cho customer
        notificationService.createCustomerNotification(
                userId,
                "REVIEW_DELETED",
                reviewId,
                String.format("Đánh giá của bạn cho sản phẩm #%d đã bị xóa do vi phạm chính sách. Nội dung: %s", productId, review.getComment()),
                null,
                4, // Ưu tiên cao
                userId, // Admin gửi
                null
        );

        reviewRepository.deleteById(reviewId);
        logger.info("Admin đã xóa đánh giá reviewId: {}", reviewId);
    }

    @Transactional
    public Review updateReview(Long reviewId, Integer userId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Điểm đánh giá phải từ 1 đến 5");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Đánh giá không tồn tại"));

        if (!review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền chỉnh sửa đánh giá này");
        }

        review.setRating(rating);
        review.setComment(comment);
        review.setIsApproved(false);
        return reviewRepository.save(review);
    }

    private ReviewDTO convertToReviewDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setReviewId(review.getReviewId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());

        userProfileRepository.findByUserId(Long.valueOf(review.getUserId())).ifPresent(profile -> {
            dto.setFullName(profile.getFullName());
            dto.setAvatarUrl(profile.getAvatarUrl());
        });

        productRepository.findById(review.getProductId()).ifPresent(product -> {
            dto.setProductName(product.getName());
        });

        return dto;
    }
}
