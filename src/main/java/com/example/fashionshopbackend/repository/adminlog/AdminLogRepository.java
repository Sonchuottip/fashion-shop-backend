package com.example.fashionshopbackend.repository.adminlog;

import com.example.fashionshopbackend.entity.adminlog.AdminLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {
}