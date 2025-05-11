package com.example.fashionshopbackend.entity.common;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "Shipping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shipping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ShippingID")
    private Long shippingId;

    @Column(name = "OrderID", nullable = false)
    private Long orderId;

    @Column(name = "Tracking_Number", length = 100)
    private String trackingNumber;

    @Column(name = "Carrier", length = 100)
    private String carrier;

    @Column(name = "Shipping_Cost", nullable = false)
    private Double shippingCost;

    @Column(name = "Shipping_Address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "Estimated_Delivery")
    private LocalDate estimatedDelivery;

    @Column(name = "Shipping_Status", length = 50)
    private String shippingStatus;
}