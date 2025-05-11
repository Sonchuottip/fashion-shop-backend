package com.example.fashionshopbackend.dto.customer;

import lombok.Data;

@Data
public class WishListDTO {
    private Long wishListId;
    private Long userId;
    private Long productId;
    private String createdAt;
}