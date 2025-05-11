package com.example.fashionshopbackend.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "Inventory_History")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HistoryID")
    private Long historyId;

    @Column(name = "VariantID", nullable = false)
    private Long variantId;

    @Column(name = "Change_Type", nullable = false)
    private String changeType; // "Add" hoặc "Remove"

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "Reason")
    private String reason;

    @Column(name = "Created_At", insertable = false, updatable = false)
    private Instant createdAt;
}