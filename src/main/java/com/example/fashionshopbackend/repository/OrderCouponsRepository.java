package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.coupon.OrderCoupons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderCouponsRepository extends JpaRepository<OrderCoupons, Integer> {
    Optional<OrderCoupons> findByOrderId(Integer orderId);
}