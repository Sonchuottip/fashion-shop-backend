package com.example.fashionshopbackend.entity.common;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Data
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "type_id", nullable = false)
    private Integer typeId;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> data;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "sender_id")
    private Integer senderId;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        isRead = false;
        priority = 1; // Mặc định ưu tiên thấp
    }

}