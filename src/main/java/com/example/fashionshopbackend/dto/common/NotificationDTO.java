package com.example.fashionshopbackend.entity.common;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class NotificationDTO {
    private Long notificationId;
    private Integer userId;
    private String message;
    private Boolean isRead;
    private OffsetDateTime createdAt;
}
