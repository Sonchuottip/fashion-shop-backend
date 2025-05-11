package com.example.fashionshopbackend.dto.common;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CouponDTO {
    private Long couponId;
    private String code;
    private Double discountPercent;
    private Double minOrderValue;
    private Integer maxUses;
    private Integer usedCount;
    private LocalDate expiryDate;
    private String createdAt; // Thêm trường createdAt với kiểu String để phù hợp với toString()
}