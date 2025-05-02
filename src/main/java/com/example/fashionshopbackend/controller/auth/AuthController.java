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
@RequestMapping("/api/auth")
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

            // Lấy user từ database để lấy role
            User user = userRepository.findByEmail(authRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));
            String jwt = jwtUtil.generateToken(user.getEmail(), user.getRole());
            logger.info("Login successful for user: {}", authRequest.getEmail());
            return ResponseEntity.ok(new AuthResponse("Login successful", jwt));
        } catch (BadCredentialsException e) {
            logger.error("Invalid credentials for email: {}", authRequest.getEmail());
            return ResponseEntity.badRequest().body(new AuthResponse("Invalid email or password", null));
        } catch (Exception e) {
            logger.error("Login error for email: {} - {}", authRequest.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse("Login failed: " + e.getMessage(), null));
        }
    }

    @GetMapping("/oauth2/{provider}/url")
    public ResponseEntity<?> getOAuth2AuthorizationUrl(@PathVariable String provider) {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(provider);
        if (clientRegistration == null) {
            logger.warn("Invalid OAuth2 provider: {}", provider);
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
            logger.error("OAuth2 callback failed with error: {}", error);
            return ResponseEntity.badRequest().body(new AuthResponse("OAuth2 login failed: " + error, null));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error("OAuth2 authentication is null or not authenticated");
            return ResponseEntity.badRequest().body(new AuthResponse("OAuth2 authentication failed", null));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after OAuth2 authentication"));
        String jwt = jwtUtil.generateToken(user.getEmail(), user.getRole());
        logger.info("OAuth2 login successful for user: {}", email);
        return ResponseEntity.ok(new AuthResponse("OAuth2 login successful", jwt));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        logger.debug("Received change password request for user: {}",
                SecurityContextHolder.getContext().getAuthentication().getName());
        try {
            authService.changePassword(request);
            logger.info("Password changed successfully for user: {}",
                    SecurityContextHolder.getContext().getAuthentication().getName());
            return ResponseEntity.ok(new AuthResponse("Password changed successfully", null));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse("Invalid input: " + e.getMessage(), null));
        } catch (IllegalStateException e) {
            logger.error("Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(403).body(new AuthResponse("Authentication failed: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Unexpected error during password change: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse("An error occurred: " + e.getMessage(), null));
        }
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
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            logger.info("Password reset successful for token: {}", request.getToken());
            return ResponseEntity.ok(new AuthResponse("Password reset successful", null));
        } catch (Exception e) {
            logger.error("Reset password failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage(), null));
        }
    }

  // Đăng xuất
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                SecurityContextHolder.clearContext();
                logger.info("Đăng xuất thành công cho người dùng: {}", authentication.getName());
                return ResponseEntity.ok(new AuthResponse("Đăng xuất thành công", null));
            } else {
                logger.warn("Không tìm thấy phiên hoạt động để đăng xuất");
                return ResponseEntity.badRequest().body(new AuthResponse("Không có phiên hoạt động", null));
            }
        } catch (Exception e) {
            logger.error("Đăng xuất thất bại: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse("Đăng xuất thất bại: " + e.getMessage(), null));
        }
    }

    // Lấy thông tin tài khoản
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Người dùng chưa được xác thực");
                return ResponseEntity.status(403).body(new AuthResponse("Người dùng chưa được xác thực", null));
            }

            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            logger.info("Lấy thông tin hồ sơ thành công cho người dùng: {}", email);
            return ResponseEntity.ok(user); // Giả sử có thể trả về trực tiếp entity User hoặc cần ánh xạ sang DTO
        } catch (Exception e) {
            logger.error("Không thể lấy thông tin hồ sơ: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse("Không thể lấy thông tin hồ sơ: " + e.getMessage(), null));
        }
    }

    // Cập nhật thông tin cá nhân
    @PutMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserUpdateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Người dùng chưa được xác thực để cập nhật hồ sơ");
                return ResponseEntity.status(403).body(new AuthResponse("Người dùng chưa được xác thực", null));
            }

            String email = authentication.getName();
            User updatedUser = authService.updateProfile(email, request);
            logger.info("Cập nhật hồ sơ thành công cho người dùng: {}, với thông tin: fullName={}, phoneNumber={}, address={}",
                    email, request.getFullName(), request.getPhoneNumber(), request.getAddress());
            return ResponseEntity.ok(new AuthResponse("Cập nhật hồ sơ thành công", null));
        } catch (IllegalArgumentException e) {
            logger.error("Dữ liệu không hợp lệ khi cập nhật hồ sơ: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse("Dữ liệu không hợp lệ: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Không thể cập nhật hồ sơ: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse("Không thể cập nhật hồ sơ: " + e.getMessage(), null));
        }
    }
}