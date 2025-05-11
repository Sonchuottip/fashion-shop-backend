package com.example.fashionshopbackend.dto.customer;

import lombok.Data;

@Data
public class CartDTO {

    private Long cartId;
    private Long userId;
    private Long variantId;
    private Integer quantity;
}