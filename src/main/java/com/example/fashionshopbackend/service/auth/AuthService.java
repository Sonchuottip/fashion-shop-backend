package com.example.fashionshopbackend.service.auth;

import com.example.fashionshopbackend.dto.auth.*;
import com.example.fashionshopbackend.entity.auth.User;
import com.example.fashionshopbackend.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mail.SimpleMailMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    // Lưu tạm OTP (sử dụng HashMap, có thể thay bằng Redis)
    private Map<String, String> otpStore = new HashMap<>();
    private static final long OTP_EXPIRY_TIME = 10 * 60 * 1000; // 10 phút

    public User register(AuthRequest authRequest) {
        if (userRepository.existsByEmail(authRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(authRequest.getEmail());
        String encodedPassword = passwordEncoder.encode(authRequest.getPassword());
        System.out.println("Encoded Password: " + encodedPassword);  // Debug
        user.setPasswordHash(encodedPassword);
        user.setFullName(authRequest.getFullName());
        System.out.println("Full Name: " + authRequest.getFullName());
        user.setRole("Customer");
        user.setProvider("local");
        System.out.println("Saved user: " + user.getEmail() + ", id: " + user.getUserId());
        return userRepository.save(user);
    }

    public boolean changePassword(ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("User found: " + user.getEmail());
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return true;
    }

    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tạo mã OTP ngẫu nhiên
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(request.getEmail(), otp);

        // Gửi email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(request.getEmail());
        message.setSubject("Password Reset OTP");
        message.setText("Your OTP for password reset is: " + otp + ". It is valid for 10 minutes.");
        mailSender.send(message);

        return "OTP sent to " + request.getEmail();
    }

    public boolean resetPassword(ResetPasswordRequest request) {
        String storedOtp = otpStore.get(request.getEmail());
        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Xóa OTP sau khi sử dụng
        otpStore.remove(request.getEmail());
        return true;
    }

    public User updateProfile(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        // Cập nhật các trường thông tin từ UserUpdateRequest
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        // Không cập nhật email, passwordHash, role, provider, providerId, avatarUrl qua API này
        // (nếu cần, có thể mở rộng logic sau)

        // Lưu và trả về người dùng đã cập nhật
        return userRepository.save(user);
    }
}