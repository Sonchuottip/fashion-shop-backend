package com.example.fashionshopbackend.service;

import com.example.fashionshopbackend.dto.AuthRequest;
import com.example.fashionshopbackend.entity.User;
import com.example.fashionshopbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(AuthRequest authRequest) {
        if (userRepository.existsByEmail(authRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(authRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(authRequest.getPassword()));
        user.setFullName(authRequest.getFullName());
        user.setRole("Customer");
        user.setProvider("local");

        return userRepository.save(user);
    }
}