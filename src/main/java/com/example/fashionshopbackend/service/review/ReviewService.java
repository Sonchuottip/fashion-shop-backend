package com.example.fashionshopbackend.service.review;

import com.example.fashionshopbackend.dto.review.ReviewDTO;
import com.example.fashionshopbackend.entity.review.Review;
import com.example.fashionshopbackend.repository.review.ReviewRepository;
import com.example.fashionshopbackend.util.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private JWTUtil jwtUtil;

    public ReviewDTO addReview(ReviewDTO dto) {
        Long userId = getCurrentUserId();
        if (dto.getRating() < 1 || dto.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(dto.getProductId());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setIsApproved(false);
        review = reviewRepository.save(review);
        return convertToDTO(review);
    }

    public List<ReviewDTO> getApprovedReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdAndIsApproved(productId, true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private Long getCurrentUserId() {
        String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid token: " + e.getMessage());
        }
    }

    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setReviewId(review.getReviewId());
        dto.setUserId(review.getUserId());
        dto.setProductId(review.getProductId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setIsApproved(review.getIsApproved());
        dto.setCreatedAt(review.getCreatedAt() != null ?
                review.getCreatedAt().toString() : null);
        return dto;
    }
}