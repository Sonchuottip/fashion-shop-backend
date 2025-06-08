package com.example.fashionshopbackend.dto.common;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
public class NotificationDTO {
    private Long notificationId;
    private Integer userId;
    private Integer typeId;
    private Long referenceId;
    private String message;
    private Map<String, Object> data;
    private Integer priority;
    private Integer senderId;
    private Boolean isRead;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;

}
