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
    @Column(name = "OrderCouponID")
    private Long orderCouponId;

    @Column(name = "OrderID", nullable = false)
    private Long orderId;

    @Column(name = "CouponID", nullable = false)
    private Long couponId;
}