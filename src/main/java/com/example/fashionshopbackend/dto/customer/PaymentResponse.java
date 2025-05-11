package com.example.fashionshopbackend.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String message;
    private String qrCodeUrl; // Cho ZaloPay
    private String transactionId; // Cho ZaloPay
    private String status; // "PENDING", "SUCCESS", "FAILED"

}