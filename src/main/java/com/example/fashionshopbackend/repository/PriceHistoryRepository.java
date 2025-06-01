package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.history.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findByVariantId(Integer variantId);

    @Query("SELECT p FROM PriceHistory p WHERE DATE(p.changedAt) = :date")
    List<PriceHistory> findByChangedAtDate(LocalDate date);
}