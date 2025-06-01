package com.example.fashionshopbackend.entity.auth;


import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.Data;

import java.time.Instant;

@Enabled
@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email",  unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash",length = 255)
    private String passwordHash;


    @Column(length = 20,name = "provider")
    private String provider = "local";

    @Column(length = 100,name = "provider_id")
    private String providerId;

    @Column(length = 20)
    private String role = "customer";

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false)
    private Instant updatedAt;


}

