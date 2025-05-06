package com.example.fashionshopbackend.entity.order;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderID")
    private Long orderId;

    @Column(name = "UserID", nullable = false)
    private Long userId;

    @Column(name = "SubTotal", nullable = false)
    private Double subTotal;

    @Column(name = "ShippingCost", nullable = false)
    private Double shippingCost;

    @Column(name = "DiscountAmount")
    private Double discountAmount;

    @Column(name = "TotalAmount", nullable = false)
    private Double totalAmount;

    @Column(name = "OrderStatus", length = 50)
    private String orderStatus; // "PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"

    @Column(name = "PaymentStatus", length = 50)
    private String paymentStatus; // "PENDING", "PAID", "COD"

    @Column(name = "ReceiverName", length = 100)
    private String receiverName;

    @Column(name = "ShippingAddress", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "PhoneNumber", length = 15)
    private String phoneNumber;
}