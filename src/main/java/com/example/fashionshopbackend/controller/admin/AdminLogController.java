package com.example.fashionshopbackend.controller.admin;

import com.example.fashionshopbackend.dto.admin.AdminLogDTO;
import com.example.fashionshopbackend.service.admin.AdminLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/admin/logs")
public class AdminLogController {

    @Autowired
    private AdminLogService adminLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Page<AdminLogDTO>> getAdminLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer adminId,
            @RequestParam(required = false) OffsetDateTime startTime,
            @RequestParam(required = false) OffsetDateTime endTime) {
        Page<AdminLogDTO> logs = adminLogService.getAdminLogs(page, size, adminId, startTime, endTime);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/by-date")
    public ResponseEntity<Page<AdminLogDTO>> getAdminLogsByDate(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam LocalDate date,
            @RequestParam(required = false) Integer adminId) {
        Page<AdminLogDTO> logs = adminLogService.getAdminLogsByDate(page, size, date, adminId);
        return ResponseEntity.ok(logs);
    }
}