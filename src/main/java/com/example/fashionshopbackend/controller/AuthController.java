package com.example.fashionshopbackend.controller;

import com.example.fashionshopbackend.dto.AuthRequest;
import com.example.fashionshopbackend.dto.AuthResponse;
import com.example.fashionshopbackend.entity.User;
import com.example.fashionshopbackend.service.AuthService;
import com.example.fashionshopbackend.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest authRequest) {
        try {
            User user = authService.register(authRequest);
            return ResponseEntity.ok(new AuthResponse("Registration successful", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtil.generateToken(authRequest.getEmail());
            return ResponseEntity.ok(new AuthResponse("Login successful", jwt));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthResponse("Invalid email or password", null));
        }
    }
}