package com.example.fashionshopbackend.dto.order;

import com.example.fashionshopbackend.dto.payment.PaymentDTO;
import com.example.fashionshopbackend.dto.shipping.ShippingDTO;
import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {

    private Long orderId;
    private Long userId;
    private Double subTotal;
    private Double shippingCost;
    private Double discountAmount;
    private Double totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private String receiverName;
    private String shippingAddress;
    private String phoneNumber;
    private List<OrderDetailDTO> orderDetails;
    private PaymentDTO payment;
    private ShippingDTO shipping;
}