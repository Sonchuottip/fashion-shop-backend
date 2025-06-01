package com.example.fashionshopbackend.dto.auth;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
    private String fullName;
    private String role;
}
