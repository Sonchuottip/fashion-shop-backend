package com.example.fashionshopbackend.entity.shipping;

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

    @Column(name = "TrackingNumber", length = 100)
    private String trackingNumber;

    @Column(name = "Carrier", length = 100)
    private String carrier;

    @Column(name = "ShippingCost", nullable = false)
    private Double shippingCost;

    @Column(name = "ShippingAddress", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "EstimatedDelivery")
    private LocalDate estimatedDelivery;

    @Column(name = "ShippingStatus", length = 50)
    private String shippingStatus;
}