package com.example.fashionshopbackend.dto.common;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ShippingDTO {

    private Long shippingId;
    private Long orderId;
    private String trackingNumber;
    private String carrier;
    private Double shippingCost;
    private String shippingAddress;
    private LocalDate estimatedDelivery;
    private String shippingStatus;
}