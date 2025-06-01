package com.example.fashionshopbackend.controller.auth;

import com.example.fashionshopbackend.dto.auth.*;
import com.example.fashionshopbackend.entity.auth.User;
import com.example.fashionshopbackend.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;


    @PostMapping("/register")
    public CompletableFuture<ResponseEntity<User>> register(@RequestBody AuthRequest authRequest) {
        return authService.register(authRequest)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable -> ResponseEntity.status(400).body(null));
    }

    @PostMapping("login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        AuthResponse response = authService.login(authRequest.getEmail(), authRequest.getPassword());
        if (response.getToken() == null) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            String response = authService.forgotPassword(request);
            logger.info("Forgot password request processed for email: {}", request.getEmail());
            return ResponseEntity.ok(new AuthResponse(response, null));
        } catch (Exception e) {
            logger.error("Forgot password failed for email: {} - {}", request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/reset-password")
    public CompletableFuture<ResponseEntity<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request)
                .thenApply(result -> ResponseEntity.ok("Password reset successful"))
                .exceptionally(throwable -> ResponseEntity.status(400).body(throwable.getMessage()));
    }

    @GetMapping("/check-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponse> checkToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            AuthResponse response = new AuthResponse("Không tìm thấy token", null);
            return ResponseEntity.status(401).body(response);
        }

        String token = authHeader.substring(7);
        AuthResponse response = authService.checkTokenStatus(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponse> logout() {
        AuthResponse response = authService.logout();
        return ResponseEntity.ok(response);
    }
}
