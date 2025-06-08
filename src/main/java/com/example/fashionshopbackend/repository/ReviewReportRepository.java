package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.review.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
}