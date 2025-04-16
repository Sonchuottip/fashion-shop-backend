package com.example.fashionshopbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;



@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    public User(String username, String password) {
    }
}