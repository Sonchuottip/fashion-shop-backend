package com.example.fashionshopbackend.entity.customer;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Order_Coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Order_Coupon_ID")
    private Long orderCouponId;

    @Column(name = "Order_ID", nullable = false)
    private Long orderId;

    @Column(name = "Coupon_ID", nullable = false)
    private Long couponId;
}