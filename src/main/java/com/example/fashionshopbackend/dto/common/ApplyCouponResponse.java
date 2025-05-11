package com.example.fashionshopbackend.dto.common;

import lombok.Data;

@Data
public class ApplyCouponResponse {
    private String message;
    private Double discountAmount;
    private Double totalAmountAfterDiscount;
}