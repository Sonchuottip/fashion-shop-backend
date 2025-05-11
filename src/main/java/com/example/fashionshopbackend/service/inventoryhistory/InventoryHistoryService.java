package com.example.fashionshopbackend.service.inventoryhistory;

import com.example.fashionshopbackend.dto.inventory.InventoryHistoryDTO;
import com.example.fashionshopbackend.entity.inventory.InventoryHistory;
import com.example.fashionshopbackend.repository.inventoryhistory.InventoryHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryHistoryService.class);

    @Autowired
    private InventoryHistoryRepository inventoryHistoryRepository;

    public List<InventoryHistoryDTO> getInventoryHistoryByVariant(Long variantId) {
        logger.debug("Fetching inventory history for variant ID: {}", variantId);
        return inventoryHistoryRepository.findByVariantId(variantId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Integer getCurrentInventory(Long variantId) {
        logger.debug("Calculating current inventory for variant ID: {}", variantId);

        List<InventoryHistory> histories = inventoryHistoryRepository.findByVariantId(variantId);

        if (histories.isEmpty()) {
            logger.debug("No inventory history found for variant ID: {}. Returning 0.", variantId);
            return 0;
        }

        int currentQuantity = 0;
        for (InventoryHistory history : histories) {
            if ("Add".equalsIgnoreCase(history.getChangeType())) {
                currentQuantity += history.getQuantity();
            } else if ("Remove".equalsIgnoreCase(history.getChangeType())) {
                currentQuantity -= history.getQuantity();
            } else {
                logger.warn("Invalid ChangeType '{}' for history ID: {}",
                        history.getChangeType(), history.getHistoryId());
            }
        }

        if (currentQuantity < 0) {
            logger.error("Calculated inventory is negative for variant ID: {}", variantId);
            throw new IllegalStateException("Inventory cannot be negative for variant ID: " + variantId);
        }

        logger.debug("Current inventory for variant ID {}: {}", variantId, currentQuantity);
        return currentQuantity;
    }

    public void updateInventory(Long variantId, Integer quantity, String reason) {
        logger.debug("Updating inventory for variant ID: {}, quantity: {}, reason: {}",
                variantId, quantity, reason);

        // Kiểm tra đầu vào
        if (quantity == null || quantity == 0) {
            logger.error("Invalid quantity: {}", quantity);
            throw new IllegalArgumentException("Quantity must be non-zero");
        }

        // Xác định ChangeType và số lượng tuyệt đối
        String changeType = quantity > 0 ? "Add" : "Remove";
        int absoluteQuantity = Math.abs(quantity);

        // Kiểm tra đủ kho nếu giảm
        if (quantity < 0) {
            int currentInventory = getCurrentInventory(variantId);
            if (currentInventory + quantity < 0) {
                logger.error("Insufficient inventory for variant ID: {}. Current: {}, Requested: {}",
                        variantId, currentInventory, quantity);
                throw new IllegalArgumentException("Insufficient inventory for variant ID: " + variantId);
            }
        }

        // Lưu bản ghi lịch sử mới
        InventoryHistory history = new InventoryHistory();
        history.setVariantId(variantId);
        history.setChangeType(changeType);
        history.setQuantity(absoluteQuantity);
        history.setReason(reason);
        history.setCreatedAt(Instant.now());

        inventoryHistoryRepository.save(history);
        logger.info("Inventory updated successfully for variant ID: {}", variantId);
    }

    private InventoryHistoryDTO convertToDTO(InventoryHistory history) {
        InventoryHistoryDTO dto = new InventoryHistoryDTO();
        dto.setHistoryId(history.getHistoryId());
        dto.setVariantId(history.getVariantId());
        dto.setChangeType(history.getChangeType());
        dto.setQuantity(history.getQuantity());
        dto.setReason(history.getReason());
        dto.setCreatedAt(history.getCreatedAt() != null ?
                history.getCreatedAt().toString() : null);
        return dto;
    }
}