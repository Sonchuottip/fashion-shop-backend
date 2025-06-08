package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.common.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationTypeRepository extends JpaRepository<NotificationType, Integer> {
    NotificationType findByTypeId(Integer typeId);
    NotificationType findByTypeName(String typeName);
}