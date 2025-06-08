package com.example.fashionshopbackend.service.common;

import com.example.fashionshopbackend.dto.common.NotificationDTO;
import com.example.fashionshopbackend.dto.common.PagedResponse;
import com.example.fashionshopbackend.entity.common.*;
import com.example.fashionshopbackend.repository.NotificationRepository;
import com.example.fashionshopbackend.repository.NotificationTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

    public void createAdminNotification(Integer adminId, String typeName, Long referenceId, String message, Map<String, Object> data, Integer priority, Integer senderId, OffsetDateTime expiresAt) {
        Integer typeId = getTypeIdByNameAndRole(typeName, "admin");
        createNotification(adminId, typeId, referenceId, message, data, priority, senderId, expiresAt);
    }

    public void createCustomerNotification(Integer customerId, String typeName, Long referenceId, String message, Map<String, Object> data, Integer priority, Integer senderId, OffsetDateTime expiresAt) {
        Integer typeId = getTypeIdByNameAndRole(typeName, "customer");
        createNotification(customerId, typeId, referenceId, message, data, priority, senderId, expiresAt);
    }

    private Integer getTypeIdByNameAndRole(String typeName, String expectedRole) {
        NotificationType type = notificationTypeRepository.findByTypeName(typeName);
        if (type == null) {
            throw new IllegalArgumentException("Notification type not found: " + typeName);
        }
        if (!type.getRecipientRole().equalsIgnoreCase(expectedRole)) {
            throw new IllegalArgumentException(
                    String.format("Notification type %s is not allowed for role %s", typeName, expectedRole)
            );
        }
        return type.getTypeId();
    }

    private void createNotification(Integer userId, Integer typeId, Long referenceId, String message, Map<String, Object> data, Integer priority, Integer senderId, OffsetDateTime expiresAt) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTypeId(typeId);
        notification.setReferenceId(referenceId);
        notification.setMessage(message);
        notification.setData(data);
        notification.setPriority(priority != null ? priority : 1);
        notification.setSenderId(senderId);
        notification.setExpiresAt(expiresAt);
        notificationRepository.save(notification);
        logger.info("Đã lưu thông báo cho userId: {}, typeId: {}", userId, typeId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<NotificationDTO> getAdminNotifications(Integer adminId, int page, int size) {
        return getNotificationsByRole(adminId, "admin", page, size);
    }

    @Transactional(readOnly = true)
    public PagedResponse<NotificationDTO> getCustomerNotifications(Integer customerId, int page, int size) {
        return getNotificationsByRole(customerId, "customer", page, size);
    }

    private PagedResponse<NotificationDTO> getNotificationsByRole(Integer userId, String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository.findByUserIdAndRecipientRole(userId, role, pageable);

        List<NotificationDTO> notificationDTOs = notificationPage.getContent().stream()
                .map(this::convertToNotificationDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                notificationDTOs,
                page,
                size,
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages()
        );
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Thông báo không tồn tại"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
        logger.info("Đã đánh dấu thông báo notificationId: {} là đã đọc", notificationId);
    }

    @Transactional(readOnly = true)
    public long countUnreadAdminNotifications(Integer adminId) {
        return notificationRepository.countByUserIdAndIsReadFalseAndRecipientRole(adminId, "admin");
    }

    @Transactional(readOnly = true)
    public long countUnreadCustomerNotifications(Integer customerId) {
        return notificationRepository.countByUserIdAndIsReadFalseAndRecipientRole(customerId, "customer");
    }

    private NotificationDTO convertToNotificationDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationId(notification.getNotificationId());
        dto.setUserId(notification.getUserId());
        dto.setTypeId(notification.getTypeId());
        dto.setReferenceId(notification.getReferenceId());
        dto.setMessage(notification.getMessage());
        dto.setData(notification.getData());
        dto.setPriority(notification.getPriority());
        dto.setSenderId(notification.getSenderId());
        dto.setIsRead(notification.getIsRead());
        dto.setExpiresAt(notification.getExpiresAt());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}