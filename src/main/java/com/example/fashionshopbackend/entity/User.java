package com.example.fashionshopbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String passwordHash;

    @Column(length = 15)
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 20)
    private String role = "Customer";

    @Column(length = 20)
    private String provider = "local";

    @Column(length = 100)
    private String providerId;

    @Column(length = 255)
    private String avatarUrl;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}