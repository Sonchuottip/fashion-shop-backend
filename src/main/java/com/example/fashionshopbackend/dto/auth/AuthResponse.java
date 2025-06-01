package com.example.fashionshopbackend.dto.auth;

import lombok.Data;

@Data
public class AuthResponse {
    private String message;
    private String token;

    public AuthResponse(String message, String token) {
        this.message = message;
        this.token = token;
    }
}
