package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByUserIdAndProductId(Integer userId, Integer productId);

    Page<Review> findByProductIdAndIsApprovedTrue(Integer productId, Pageable pageable);

    Page<Review> findByProductIdAndRatingAndIsApprovedTrue(Integer productId, Integer rating, Pageable pageable);

    Page<Review> findByUserId(Integer userId, Pageable pageable);

    Page<Review> findByIsApprovedFalse(Pageable pageable);
}