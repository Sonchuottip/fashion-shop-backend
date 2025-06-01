package com.example.fashionshopbackend.controller.admin;

import com.example.fashionshopbackend.entity.history.*;
import com.example.fashionshopbackend.repository.PriceHistoryRepository;
import com.example.fashionshopbackend.repository.InventoryHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminHistory {

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private InventoryHistoryRepository inventoryHistoryRepository;

    // API lấy lịch sử giá theo ngày
    @GetMapping("/price-history/date")
    public ResponseEntity<List<PriceHistory>> getPriceHistoryByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<PriceHistory> priceHistories = priceHistoryRepository.findByChangedAtDate(date);
        return ResponseEntity.ok(priceHistories);
    }

    // API lấy lịch sử giá theo variant_id
    @GetMapping("/price-history/variant/{variantId}")
    public ResponseEntity<List<PriceHistory>> getPriceHistoryByVariantId(@PathVariable Integer variantId) {
        List<PriceHistory> priceHistories = priceHistoryRepository.findByVariantId(variantId);
        return ResponseEntity.ok(priceHistories);
    }

    // API lấy lịch sử tồn kho theo variant_id
    @GetMapping("/inventory-history/variant/{variantId}")
    public ResponseEntity<List<InventoryHistory>> getInventoryHistoryByVariantId(@PathVariable Integer variantId) {
        List<InventoryHistory> inventoryHistories = inventoryHistoryRepository.findByVariantId(variantId);
        return ResponseEntity.ok(inventoryHistories);
    }

    // API tạo bản ghi lịch sử tồn kho
    @PostMapping("/inventory-history")
    public ResponseEntity<InventoryHistory> createInventoryHistory(@RequestBody InventoryHistory inventoryHistory) {
        InventoryHistory savedHistory = inventoryHistoryRepository.save(inventoryHistory);
        return ResponseEntity.ok(savedHistory);
    }

    // API sửa bản ghi lịch sử tồn kho
    @PutMapping("/inventory-history/{historyId}")
    public ResponseEntity<InventoryHistory> updateInventoryHistory(
            @PathVariable Long historyId,
            @RequestBody InventoryHistory updatedHistory) {
        return inventoryHistoryRepository.findById(historyId)
                .map(history -> {
                    history.setVariantId(updatedHistory.getVariantId());
                    history.setChangeType(updatedHistory.getChangeType());
                    history.setQuantity(updatedHistory.getQuantity());
                    history.setReason(updatedHistory.getReason());
                    InventoryHistory savedHistory = inventoryHistoryRepository.save(history);
                    return ResponseEntity.ok(savedHistory);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // API xóa bản ghi lịch sử tồn kho
    @DeleteMapping("/inventory-history/{historyId}")
    public ResponseEntity<Void> deleteInventoryHistory(@PathVariable Long historyId) {
        if (inventoryHistoryRepository.existsById(historyId)) {
            inventoryHistoryRepository.deleteById(historyId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}