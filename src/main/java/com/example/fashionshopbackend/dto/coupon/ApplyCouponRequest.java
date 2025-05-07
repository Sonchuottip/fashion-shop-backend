package com.example.fashionshopbackend.dto.coupon;

import lombok.Data;

@Data
public class ApplyCouponRequest {
    private Long orderId;
    private String couponCode;
}