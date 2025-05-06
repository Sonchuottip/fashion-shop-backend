package com.example.fashionshopbackend.dto.payment;

import lombok.Data;

@Data
public class PaymentDTO {

    private Long paymentId;
    private Long orderId;
    private String paymentMethod;
    private String transactionId;
    private String paymentStatus;
}