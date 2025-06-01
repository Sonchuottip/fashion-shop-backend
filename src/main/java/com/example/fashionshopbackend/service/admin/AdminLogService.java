package com.example.fashionshopbackend.service.admin;

import com.example.fashionshopbackend.dto.admin.AdminLogDTO;
import com.example.fashionshopbackend.entity.admin.AdminLog;
import com.example.fashionshopbackend.entity.auth.User;
import com.example.fashionshopbackend.repository.AdminLogRepository;
import com.example.fashionshopbackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminLogService {

    private final static Logger logger = LoggerFactory.getLogger(AdminLogService.class);

    @Autowired
    private AdminLogRepository adminLogRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<AdminLogDTO> getAdminLogs(int page, int size, Integer adminId, OffsetDateTime startTime, OffsetDateTime endTime) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timeStamp"));

        Specification<AdminLog> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (adminId != null) {
                predicates.add(criteriaBuilder.equal(root.get("adminId"), adminId));
            }
            if (startTime != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timeStamp"), startTime));
            }
            if (endTime != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timeStamp"), endTime));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return adminLogRepository.findAll(specification, pageable)
                .map(this::convertToDTO);
    }

    public Page<AdminLogDTO> getAdminLogsByDate(int page, int size, LocalDate date, Integer adminId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timeStamp"));

        // Chuyển ngày thành khoảng thời gian: từ 00:00:00 đến 23:59:59
        OffsetDateTime startOfDay = date.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime endOfDay = date.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

        Specification<AdminLog> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.between(root.get("timeStamp"), startOfDay, endOfDay));
            if (adminId != null) {
                predicates.add(criteriaBuilder.equal(root.get("adminId"), adminId));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return adminLogRepository.findAll(specification, pageable)
                .map(this::convertToDTO);
    }

    private AdminLogDTO convertToDTO(AdminLog adminLog) {
        AdminLogDTO dto = new AdminLogDTO();
        dto.setLogId(adminLog.getLogId());
        dto.setAdminId(adminLog.getAdminId());
        dto.setAction(adminLog.getAction());
        dto.setTimeStamp(adminLog.getTimeStamp());
        return dto;
    }

    @Transactional
    public void logAdminAction(String action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

            AdminLog adminLog = new AdminLog();
            adminLog.setAdminId(Math.toIntExact(user.getId()));
            adminLog.setAction(action);
            adminLog.setTimeStamp(OffsetDateTime.now());

            try {
                adminLogRepository.save(adminLog);
                logger.info("Đã lưu log admin: adminId={}, action={}", user.getId(), action);
            } catch (Exception e) {
                logger.error("Lỗi khi lưu log admin: {}", e.getMessage());
            }
        }
    }
}