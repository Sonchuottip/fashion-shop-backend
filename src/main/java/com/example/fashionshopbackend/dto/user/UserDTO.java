package com.example.fashionshopbackend.dto.user;

import lombok.Data;

@Data
public class UserDTO {
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String role;
}