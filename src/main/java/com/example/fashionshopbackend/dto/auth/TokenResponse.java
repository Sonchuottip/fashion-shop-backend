package com.example.fashionshopbackend.dto.auth;

import lombok.Data;

@Data
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String email;
    private String role;

    public TokenResponse(String accessToken, String refreshToken, String email, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.role = role;
    }
}