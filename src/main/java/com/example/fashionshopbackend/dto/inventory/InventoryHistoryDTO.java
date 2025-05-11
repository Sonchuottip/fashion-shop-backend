package com.example.fashionshopbackend.dto.inventory;

import lombok.Data;

@Data
public class InventoryHistoryDTO {
    private Long historyId;
    private Long variantId;
    private String changeType;
    private Integer quantity;
    private String reason;
    private String createdAt;
}