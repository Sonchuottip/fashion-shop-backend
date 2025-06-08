package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Integer> {
    Optional<Coupon> findByCodeAndIsActiveTrue(String code);
}