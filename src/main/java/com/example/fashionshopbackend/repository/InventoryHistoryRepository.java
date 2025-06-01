package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.history.InventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Long> {
    List<InventoryHistory> findByVariantId(Integer variantId);
}