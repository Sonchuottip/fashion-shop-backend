package com.example.fashionshopbackend.repository.ordercoupon;

import com.example.fashionshopbackend.entity.customer.OrderCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderCouponRepository extends JpaRepository<OrderCoupon, Long> {

    Optional<OrderCoupon> findByOrderId(Long orderId);
}