package com.example.fashionshopbackend.dto.order;

import lombok.Data;

@Data
public class OrderDetailDTO {

    private Long orderDetailId;
    private Long orderId;
    private Long variantId;
    private Integer quantity;
    private Double price;
}