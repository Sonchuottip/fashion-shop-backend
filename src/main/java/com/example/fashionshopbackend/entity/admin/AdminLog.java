package com.example.fashionshopbackend.entity.admin;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Data
@Table(name = "admin_logs")
public class AdminLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "admin_id", nullable = false)
    private Integer adminId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "time_stamp", nullable = false)
    private OffsetDateTime timeStamp;
}