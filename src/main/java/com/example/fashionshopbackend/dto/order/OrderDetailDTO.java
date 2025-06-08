package com.example.fashionshopbackend.dto.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDetailDTO {
    private Integer orderDetailId;
    private Integer variantId;
    private Integer quantity;
    private BigDecimal price;
}
