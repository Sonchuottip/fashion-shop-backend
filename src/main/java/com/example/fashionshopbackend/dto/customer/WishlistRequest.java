package com.example.fashionshopbackend.dto.customer;

import lombok.Data;

@Data
public class WishlistRequest {
    private Long userId;
    private Integer productId;
}
