package com.example.fashionshopbackend.controller.auth;

import com.example.fashionshopbackend.dto.auth.*;
import com.example.fashionshopbackend.entity.auth.RefreshToken;
import com.example.fashionshopbackend.entity.auth.User;
import com.example.fashionshopbackend.repository.UserRepository;
import com.example.fashionshopbackend.service.auth.AuthService;
import com.example.fashionshopbackend.service.auth.RefreshTokenService;
import com.example.fashionshopbackend.util.JWTUtil;
import com.nimbusds.jose.JOSEException;
import com.example.fashionshopbackend.dto.auth.RefreshTokenRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;
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

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest, HttpServletResponse response) {
        Map<String, Object> loginResult = authService.login(authRequest.getEmail(), authRequest.getPassword());
        AuthResponse authResponse = (AuthResponse) loginResult.get("authResponse");

        if (authResponse.getToken() != null) {
            String refreshToken = (String) loginResult.get("refreshToken");
            Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(true);
            refreshCookie.setPath("/api/auth");
            refreshCookie.setMaxAge(604800); // 7 ngày
            refreshCookie.setAttribute("SameSite", "Strict"); // Jakarta EE không hỗ trợ setSameSite
            response.addCookie(refreshCookie);
            logger.info("Set refresh token cookie for user: {}", authRequest.getEmail());
        }

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request) {
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Refresh token cookie not found");
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found");
                });

        AuthResponse authResponse = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(authResponse);
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
    public ResponseEntity<AuthResponse> logout(HttpServletResponse response) {
        AuthResponse authResponse = authService.logout();

        Cookie refreshCookie = new Cookie("refresh_token", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/api/auth");
        refreshCookie.setMaxAge(0); // Xóa cookie
        refreshCookie.setAttribute("SameSite", "Strict");
        response.addCookie(refreshCookie);
        logger.info("Cleared refresh token cookie");

        return ResponseEntity.ok(authResponse);
    }
}
