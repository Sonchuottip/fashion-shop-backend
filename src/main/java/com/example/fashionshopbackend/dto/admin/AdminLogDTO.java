package com.example.fashionshopbackend.dto.admin;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AdminLogDTO {

    private Long logId;
    private Integer adminId;
    private String action;
    private OffsetDateTime timeStamp;
}