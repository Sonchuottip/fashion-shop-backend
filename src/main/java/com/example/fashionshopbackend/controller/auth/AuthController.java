package com.example.fashionshopbackend.controller.auth;

import com.example.fashionshopbackend.dto.auth.*;
import com.example.fashionshopbackend.entity.auth.User;
import com.example.fashionshopbackend.repository.user.UserRepository;
import com.example.fashionshopbackend.service.auth.AuthService;
import com.example.fashionshopbackend.util.jwt.JWTUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Sửa mapping để khớp với request
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest authRequest) {
        try {
            User user = authService.register(authRequest);
            return ResponseEntity.ok(new AuthResponse("Registration successful", null));
        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage(), e);
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
        } catch (BadCredentialsException e) {
            logger.error("Invalid credentials: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AuthResponse("Invalid email or password", null));
        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AuthResponse("Invalid email or password", null));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        logger.debug("Received change password request: oldPassword={}, newPassword={}",
                request.getOldPassword(), request.getNewPassword());
        try {
            authService.changePassword(request);
            logger.info("Password changed successfully for user: {}",
                    SecurityContextHolder.getContext().getAuthentication().getName());
            return ResponseEntity.ok(new AuthResponse("Password changed successfully", null));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AuthResponse("Invalid input: " + e.getMessage(), null));
        } catch (IllegalStateException e) {
            logger.error("Authentication failed: {}", e.getMessage(), e);
            return ResponseEntity.status(403).body(new AuthResponse("Authentication failed: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Unexpected error during password change: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AuthResponse("An error occurred: " + e.getMessage(), null));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            String response = authService.forgotPassword(request);
            return ResponseEntity.ok(new AuthResponse(response, null));
        } catch (Exception e) {
            logger.error("Forgot password failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(new AuthResponse("Password reset successful", null));
        } catch (Exception e) {
            logger.error("Reset password failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/oauth2/{provider}/url")
    public ResponseEntity<?> getOAuth2AuthorizationUrl(@PathVariable String provider) {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(provider);
        if (clientRegistration == null) {
            logger.warn("Invalid provider: {}", provider);
            return ResponseEntity.badRequest().body(new AuthResponse("Invalid provider: " + provider, null));
        }

        String authorizationUri = clientRegistration.getProviderDetails().getAuthorizationUri();
        String clientId = clientRegistration.getClientId();
        String redirectUri = clientRegistration.getRedirectUri();
        String scope = String.join(" ", clientRegistration.getScopes());

        String authorizationUrl = String.format("%s?client_id=%s&redirect_uri=%s&scope=%s&response_type=code",
                authorizationUri, clientId, redirectUri, scope);
        logger.debug("Generated OAuth2 URL for provider {}: {}", provider, authorizationUrl);
        return ResponseEntity.ok(new AuthResponse("Authorization URL", authorizationUrl));
    }

    @GetMapping("/oauth2/callback")
    public ResponseEntity<?> oauth2Callback(@RequestParam(value = "code", required = false) String code,
                                            @RequestParam(value = "error", required = false) String error) {
        if (error != null) {
            logger.error("OAuth2 callback failed: {}", error);
            return ResponseEntity.badRequest().body(new AuthResponse("OAuth2 login failed: " + error, null));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error("OAuth2 authentication is null or not authenticated");
            return ResponseEntity.badRequest().body(new AuthResponse("OAuth2 authentication failed", null));
        }

        String email = authentication.getName();
        String jwt = jwtUtil.generateToken(email);
        logger.info("OAuth2 login successful for user: {}", email);
        return ResponseEntity.ok(new AuthResponse("OAuth2 login successful", jwt));
    }

    @GetMapping("/admin/test")
    public ResponseEntity<?> testAdminAccess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Admin"))) {
            logger.warn("Admin access denied for user: {}", auth != null ? auth.getName() : "unknown");
            return ResponseEntity.status(403).body(new AuthResponse("Access denied", null));
        }
        return ResponseEntity.ok("Welcome, Admin!");
    }
}