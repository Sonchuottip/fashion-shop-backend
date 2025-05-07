package com.example.fashionshopbackend.service.adminlog;

import com.example.fashionshopbackend.dto.adminlog.AdminLogDTO;
import com.example.fashionshopbackend.entity.adminlog.AdminLog;
import com.example.fashionshopbackend.repository.adminlog.AdminLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminLogService {

    @Autowired
    private AdminLogRepository adminLogRepository;

    public List<AdminLogDTO> getAllAdminLogs() {
        return adminLogRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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