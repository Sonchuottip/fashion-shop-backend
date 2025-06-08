package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.common.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserId(Integer userId, Pageable pageable);

    @Query("SELECT n FROM Notification n JOIN NotificationType nt ON n.typeId = nt.typeId WHERE n.userId = :userId AND nt.recipientRole = :role")
    Page<Notification> findByUserIdAndRecipientRole(Integer userId, String role, Pageable pageable);

    long countByUserIdAndIsReadFalse(Integer userId);

    @Query("SELECT COUNT(n) FROM Notification n JOIN NotificationType nt ON n.typeId = nt.typeId WHERE n.userId = :userId AND n.isRead = false AND nt.recipientRole = :role")
    long countByUserIdAndIsReadFalseAndRecipientRole(Integer userId, String role);
}