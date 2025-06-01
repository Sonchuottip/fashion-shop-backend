package com.example.fashionshopbackend.controller.auth;

import com.example.fashionshopbackend.dto.auth.AuthResponse;
import com.example.fashionshopbackend.dto.auth.ChangePasswordRequest;
import com.example.fashionshopbackend.dto.auth.UserUpdateRequest;
import com.example.fashionshopbackend.entity.auth.UserProfile;
import com.example.fashionshopbackend.service.auth.AuthService;
import com.example.fashionshopbackend.service.auth.PersonalService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("api/personal")
public class PersonalController {

    private static final Logger logger = LoggerFactory.getLogger(PersonalController.class);

    @Autowired
    private PersonalService personalService;


    @Autowired
    private AuthService authService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            UserProfile profile = personalService.getProfile(email);
            logger.info("Lấy thông tin hồ sơ thành công cho người dùng: {}", email);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Không thể lấy thông tin hồ sơ: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse("Không thể lấy thông tin hồ sơ: " + e.getMessage(), null));
        }
    }

    @PutMapping("/profile/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserUpdateRequest request) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            UserProfile updatedProfile = personalService.updateProfile(email, request);
            logger.info("Cập nhật hồ sơ thành công cho người dùng: {}, với thông tin: fullName={}, phoneNumber={}, address={}",
                    email, updatedProfile.getFullName(), updatedProfile.getPhoneNumber(), updatedProfile.getAddress());
            return ResponseEntity.ok(new AuthResponse("Cập nhật hồ sơ thành công", null));
        } catch (IllegalArgumentException e) {
            logger.error("Dữ liệu không hợp lệ khi cập nhật hồ sơ: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse("Dữ liệu không hợp lệ: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Không thể cập nhật hồ sơ: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse("Không thể cập nhật hồ sơ: " + e.getMessage(), null));
        }
    }

    @PostMapping("/change-password")
    public CompletableFuture<ResponseEntity<String>> changePassword(@RequestBody ChangePasswordRequest request) {
        return authService.changePassword(request)
                .thenApply(result -> ResponseEntity.ok("Password changed successfully"))
                .exceptionally(throwable -> ResponseEntity.status(400).body(throwable.getMessage()));
    }
}
