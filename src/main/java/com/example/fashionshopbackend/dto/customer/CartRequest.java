package com.example.fashionshopbackend.dto.customer;

import lombok.Data;

@Data
public class CartRequest {
    private Long userId;
    private Long variantId;
    private Integer quantity;
}
