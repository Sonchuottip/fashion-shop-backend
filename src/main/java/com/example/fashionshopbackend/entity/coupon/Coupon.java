package com.example.fashionshopbackend.entity.coupon;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Integer couponId;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "discount_type", nullable = false)
    private String discountType;

    @Column(name = "discount_percent")
    private BigDecimal discountPercent;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "min_order_value", nullable = false)
    private BigDecimal minOrderValue;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "max_uses_per_user")
    private Integer maxUsesPerUser;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

}