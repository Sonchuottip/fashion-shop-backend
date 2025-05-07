package com.example.fashionshopbackend.repository.coupon;

import com.example.fashionshopbackend.entity.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    // Tìm mã giảm giá theo mã (Code)
    Optional<Coupon> findByCode(String code);

    // Kiểm tra xem mã giảm giá có tồn tại không
    boolean existsByCode(String code);
}