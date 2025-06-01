package com.example.fashionshopbackend.service.auth;

import com.example.fashionshopbackend.dto.auth.*;
import com.example.fashionshopbackend.entity.auth.User;
import com.example.fashionshopbackend.entity.auth.UserProfile;
import com.example.fashionshopbackend.repository.UserProfileRepository;
import com.example.fashionshopbackend.repository.UserRepository;
import com.example.fashionshopbackend.util.JWTUtil;

import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private static Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final long OTP_EXPIRY_TIME = 10; // 10 phút
    private static final String OTP_PREFIX = "otp:";
    private static final String TOKEN_PREFIX = "jwt:token:";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserProfileRepository userProfileRepository;  // Giả sử đã có repository này

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public AuthResponse login(String email, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.error("User not found for email: {}", email);
                        return new RuntimeException("User not found");
                    });
            String jwt;
            try {
                jwt = jwtUtil.generateToken(user.getId(),user.getEmail(),user.getProvider());
                logger.info("JWT token: {} for user:{}", jwt,user.getEmail());
            } catch (JOSEException e) {
                logger.error("Failed to generate JWT for user {}: {}", email, e.getMessage());
                return new AuthResponse("Failed to generate token", null);
            }

            logger.info("Login successful for user: {}", email);
            return new AuthResponse("Login successful", jwt);
        } catch (BadCredentialsException e){
            logger.warn("Invalid credentials for email: {}", email);
            return new AuthResponse("Invalid email or password", null);
        } catch (Exception e) {
            logger.error("Login failed for email: {} - {}", email, e.getMessage());
            return new AuthResponse("Login failed: " + e.getMessage(), null);
        }
    }

    @Async
    @Transactional
    public CompletableFuture<User> register(AuthRequest authRequest) {
        if (userRepository.existsByEmail(authRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(authRequest.getEmail());
        String encodedPassword = passwordEncoder.encode(authRequest.getPassword());
        logger.debug("Encoded Password: {}", encodedPassword);
        user.setPasswordHash(encodedPassword);
        user.setRole("customer");
        user.setProvider("local");
        userRepository.save(user);

        user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after save"));
        long userId = user.getId();
        logger.debug("Saved user: {}, id: {}", user.getEmail(), userId);

        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        if (authRequest.getFullName() == null || authRequest.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }
        profile.setFullName(authRequest.getFullName());
        userProfileRepository.save(profile);

        return CompletableFuture.completedFuture(user);
    }

    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.error("User not found for email: {}", request.getEmail());
                    return new RuntimeException("User not found");
                });

        // Tạo mã OTP ngẫu nhiên
        String otp = String.format("%06d", new Random().nextInt(999999));
        String otpKey = OTP_PREFIX + request.getEmail();

        // Lưu OTP vào Redis với TTL 10 phút
        redisTemplate.opsForValue().set(otpKey, otp, OTP_EXPIRY_TIME, TimeUnit.MINUTES);
        logger.info("Stored OTP for email: {}", request.getEmail());

        // Gửi email bất đồng bộ
        sendOtpEmailAsync(request.getEmail(), otp);

        return "OTP sent to " + request.getEmail();
    }

    @Async
    public void sendOtpEmailAsync(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Reset OTP");
            message.setText("Your OTP for password reset is: " + otp + ". It is valid for 10 minutes.");
            mailSender.send(message);
            logger.info("Sent OTP email to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}: {}", email, e.getMessage(), e);
        }
    }

    @Async
    public CompletableFuture<Boolean> changePassword(ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        logger.info("User found: {}", user.getEmail());
        if (!passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return CompletableFuture.completedFuture(true);
    }

    @Async
    public CompletableFuture<Boolean> resetPassword(ResetPasswordRequest request) {
        String otpKey = OTP_PREFIX + request.getEmail();
        String storedOtp = redisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            logger.warn("Invalid or expired OTP for email: {}", request.getEmail());
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.error("User not found for email: {}", request.getEmail());
                    return new RuntimeException("User not found");
                });

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redisTemplate.delete(otpKey);
        logger.info("Password reset successful for email: {}", request.getEmail());

        return CompletableFuture.completedFuture(true);
    }

    public AuthResponse logout() {
        try {
            // Lấy thông tin người dùng hiện tại từ SecurityContext
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            if (email == null || "anonymousUser".equals(email)) {
                logger.warn("Không tìm thấy phiên hợp lệ để đăng xuất");
                return new AuthResponse("Không có phiên hợp lệ để đăng xuất", null);
            }

            // Tìm user dựa trên email
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                logger.error("Không tìm thấy người dùng với email: {}", email);
                return new AuthResponse("Không tìm thấy người dùng", null);
            }

            User user = userOptional.get();
            long userId = user.getId();

            // Xóa tất cả token liên quan đến userId bằng TokenService
            tokenService.revokeToken(String.valueOf(userId));
            logger.info("Đã xóa token cho người dùng: {}", email);

            // Xóa SecurityContext
            SecurityContextHolder.clearContext();
            logger.info("Đăng xuất thành công cho người dùng: {}", email);
            return new AuthResponse("Đăng xuất thành công", null);
        } catch (Exception e) {
            logger.error("Đăng xuất thất bại: {}", e.getMessage(), e);
            return new AuthResponse("Đăng xuất thất bại: " + e.getMessage(), null);
        }
    }

    public AuthResponse checkTokenStatus(String token) {
        try {
            // Kiểm tra token có hợp lệ không
            if (token == null || token.trim().isEmpty()) {
                logger.warn("Token không được cung cấp");
                return new AuthResponse("Token không hợp lệ", null);
            }

            // Kiểm tra xem token có hết hạn không
            boolean isExpired = jwtUtil.isTokenExpired(token);
            if (isExpired) {
                logger.warn("Token đã hết hạn");
                return logout(); // Tự động đăng xuất nếu token hết hạn
            }

            // Lấy userId từ token
            long userId = jwtUtil.getUserIdFromToken(token);

            // Kiểm tra xem token có bị thu hồi không
            boolean isRevoked = tokenService.isSpecificTokenRevoke(String.valueOf(userId), token);
            if (isRevoked) {
                logger.warn("Token đã bị thu hồi cho userId: {}", userId);
                return logout(); // Tự động đăng xuất nếu token bị thu hồi
            }

            logger.info("Token vẫn hợp lệ cho userId: {}", userId);
            return new AuthResponse("Token vẫn hợp lệ", null);
        } catch (JOSEException | ParseException e) {
            logger.error("Lỗi khi kiểm tra trạng thái token: {}", e.getMessage(), e);
            return new AuthResponse("Token không hợp lệ: " + e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Kiểm tra trạng thái token thất bại: {}", e.getMessage(), e);
            return new AuthResponse("Kiểm tra trạng thái token thất bại: " + e.getMessage(), null);
        }
    }
}
