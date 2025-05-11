package com.example.fashionshopbackend.dto.admin;

import lombok.Data;

@Data
public class AdminLogDTO {
    private Long logId;
    private Long adminId;
    private String action;
    private String timestamp;
}