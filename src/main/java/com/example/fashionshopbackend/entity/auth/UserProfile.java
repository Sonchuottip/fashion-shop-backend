package com.example.fashionshopbackend.entity.auth;


import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100,name = "full_name")
    private String fullName;

    @Column(length = 15,name = "phone_number")
    private String phoneNumber;

    @Column(columnDefinition = "TEXT",name = "address")
    private String address;

    @Column(length = 255,name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false)
    private Instant updatedAt;
}
