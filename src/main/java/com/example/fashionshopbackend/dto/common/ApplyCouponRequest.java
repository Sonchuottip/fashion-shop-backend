package com.example.fashionshopbackend.dto.common;

import lombok.Data;

@Data
public class ApplyCouponRequest {
    private Long orderId;
    private String couponCode;
}