package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.customer.CustomerCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerCouponRepository extends JpaRepository<CustomerCoupon, Long> {
    Optional<CustomerCoupon> findByUserIdAndCouponId(Long userId, Long couponId);
}