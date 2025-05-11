package com.example.fashionshopbackend.service.adminlog;

import com.example.fashionshopbackend.dto.admin.AdminLogDTO;
import com.example.fashionshopbackend.entity.adminlog.AdminLog;
import com.example.fashionshopbackend.repository.adminlog.AdminLogRepository;
import com.example.fashionshopbackend.util.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminLogService {

    @Autowired
    private AdminLogRepository adminLogRepository;

    @Autowired
    private JWTUtil jwtUtil;

    public List<AdminLogDTO> getAllAdminLogs() {
        return adminLogRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void logAdminAction(String action) {
        Long adminId = getCurrentUserId();
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction(action);
        adminLogRepository.save(log);
    }

    private Long getCurrentUserId() {
        String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid token: " + e.getMessage());
        }
    }

    private AdminLogDTO convertToDTO(AdminLog log) {
        AdminLogDTO dto = new AdminLogDTO();
        dto.setLogId(log.getLogId());
        dto.setAdminId(log.getAdminId());
        dto.setAction(log.getAction());
        dto.setTimestamp(log.getTimestamp() != null ? log.getTimestamp().toString() : null);
        return dto;
    }
}