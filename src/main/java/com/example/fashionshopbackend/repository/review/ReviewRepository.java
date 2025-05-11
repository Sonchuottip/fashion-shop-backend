package com.example.fashionshopbackend.repository.review;

import com.example.fashionshopbackend.entity.customer.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdAndIsApproved(Long productId, Boolean isApproved);
}