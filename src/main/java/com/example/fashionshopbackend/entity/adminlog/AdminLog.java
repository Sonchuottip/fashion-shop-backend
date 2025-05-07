package com.example.fashionshopbackend.entity.adminlog;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "AdminLogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogID")
    private Long logId;

    @Column(name = "AdminID", nullable = false)
    private Long adminId;

    @Column(name = "Action", nullable = false)
    private String action;

    @Column(name = "Timestamp", insertable = false, updatable = false)
    private Instant timestamp;
}