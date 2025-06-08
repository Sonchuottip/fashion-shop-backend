package com.example.fashionshopbackend.dto.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Integer orderId;
    private Integer userId;
    private BigDecimal subTotal;
    private BigDecimal shippingCost;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private String receiverName;
    private String shippingAddress;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderDetailDTO> orderDetails;
    private String couponCode;
    private String paymentUrl;
}