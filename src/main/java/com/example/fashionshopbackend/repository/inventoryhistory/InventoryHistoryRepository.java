package com.example.fashionshopbackend.repository.inventoryhistory;

import com.example.fashionshopbackend.entity.inventory.InventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Long> {

    List<InventoryHistory> findByVariantId(Long variantId);
}