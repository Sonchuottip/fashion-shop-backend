package com.example.fashionshopbackend.dto.coupon;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CouponDTO {
    private Integer couponId; // Dùng cho cập nhật, có thể null khi tạo mới
    private String code;
    private Double discountPercent;
    private Double minOrderValue;
    private Integer maxUses;
    private Integer usedCount;
    private LocalDate expiryDate;
}