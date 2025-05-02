package com.example.fashionshopbackend.entity.coupon;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "Coupons")
@Data
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CouponID")
    private Integer couponId;

    @Column(name = "Code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "DiscountPercent", nullable = false)
    private Double discountPercent;

    @Column(name = "MinOrderValue", nullable = false)
    private Double minOrderValue;

    @Column(name = "MaxUses")
    private Integer maxUses;

    @Column(name = "UsedCount", nullable = false)
    private Integer usedCount;

    @Column(name = "ExpiryDate", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private Instant createdAt;
}