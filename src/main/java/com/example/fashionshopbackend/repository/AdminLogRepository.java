package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.admin.AdminLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminLogRepository extends JpaRepository<AdminLog, Long>, JpaSpecificationExecutor<AdminLog> {
}