package com.example.fashionshopbackend.dto.inventoryhistory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InventoryUpdateDTO {
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity;

    @NotBlank(message = "Reason cannot be blank")
    private String reason;
}