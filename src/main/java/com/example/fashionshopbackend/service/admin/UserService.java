package com.example.fashionshopbackend.service.admin;

import com.example.fashionshopbackend.dto.user.UserDTO;
import com.example.fashionshopbackend.entity.adminlog.AdminLog;
import com.example.fashionshopbackend.entity.auth.User;
import com.example.fashionshopbackend.repository.adminlog.AdminLogRepository;
import com.example.fashionshopbackend.repository.user.UserRepository;
import com.example.fashionshopbackend.util.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminLogRepository adminLogRepository;

    @Autowired
    private JWTUtil jwtUtil;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        userRepository.deleteById(userId);
        logAdminAction("Deleted user: " + user.getEmail());
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        return dto;
    }

    private Long getCurrentUserId() {
        String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid token: " + e.getMessage());
        }
    }

    private void logAdminAction(String action) {
        Long adminId = getCurrentUserId();
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction(action);
        adminLogRepository.save(log);
    }
}