package com.example.fashionshopbackend.dto.customer;

import lombok.Data;

@Data
public class PaymentUpdateRequest {
    private String paymentStatus; // "Completed" hoặc "Failed"
}