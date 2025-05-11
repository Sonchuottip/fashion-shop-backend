package com.example.fashionshopbackend.dto.customer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OrderUpdateDTO {
    @NotNull(message = "Order status is required")
    @Pattern(regexp = "PENDING|PROCESSING|SHIPPED|DELIVERED|CANCELLED",
            message = "Order status must be one of: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED")
    private String orderStatus;
}