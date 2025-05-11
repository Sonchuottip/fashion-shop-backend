package com.example.fashionshopbackend.dto.customer;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long orderId;
    private String paymentMethod; // "ZALOPAY" hoặc "COD"
    private Double amount;
    private String description;
}