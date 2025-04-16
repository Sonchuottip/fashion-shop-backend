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
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(AuthRequest request) {
        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User(request.getUsername(), passwordEncoder.encode(request.getPassword()));
        return userRepo.save(user);
    }
}
