package com.example.fashionshopbackend.controller.customer;

import com.example.fashionshopbackend.dto.common.PagedResponse;
import com.example.fashionshopbackend.dto.common.NotificationDTO;
import com.example.fashionshopbackend.service.common.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/notifications")
public class CustomerNotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<PagedResponse<NotificationDTO>> getNotifications(
            @RequestParam Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<NotificationDTO> notifications = notificationService.getCustomerNotifications(customerId, page, size);
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok("Đánh dấu thông báo đã đọc");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Thông báo không tồn tại");
        }
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> countUnreadNotifications(@RequestParam Integer customerId) {
        long count = notificationService.countUnreadCustomerNotifications(customerId);
        return ResponseEntity.ok(count);
    }
}