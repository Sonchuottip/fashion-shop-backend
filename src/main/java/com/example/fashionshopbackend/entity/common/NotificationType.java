package com.example.fashionshopbackend.service.common;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Data
@Table(name ="notification_types")
public class NotificationType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer typeId;

    @Column(name = "type_name", nullable = false, unique = true, length = 50)
    private String typeName;

    @Column(name = "description")
    private String description;

    @Column(name = "recipient_role", nullable = false, length = 20)
    private String recipientRole;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
