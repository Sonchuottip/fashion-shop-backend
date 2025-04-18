package com.example.fashionshopbackend.dto.admin;

import lombok.Data;

@Data
public class AdminResponse {
    private String message;

    public AdminResponse(String message) {
        this.message = message;
    }
}