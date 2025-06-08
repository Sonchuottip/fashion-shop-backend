package com.example.fashionshopbackend.entity.coupon;


import jakarta.persistence.*;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(name = "coupon_usage", uniqueConstraints = @UniqueConstraint(columnNames = {"coupon_id", "user_id"}))
public class CouponUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    private Integer usageId;

    @Column(name = "coupon_id", nullable = false)
    private Integer couponId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

}