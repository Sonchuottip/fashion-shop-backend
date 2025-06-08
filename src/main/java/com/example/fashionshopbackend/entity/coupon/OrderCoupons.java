package com.example.fashionshopbackend.entity.coupon;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "order_coupons")
@Data
public class OrderCoupons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_coupon_id")
    private Integer orderCouponId;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "coupon_id", nullable = false)
    private Integer couponId;
}
