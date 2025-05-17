package com.example.fashionshopbackend.entity.customer;

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
    @Column(name = "Order_ID")
    private Long orderId;

    @Column(name = "User_ID", nullable = false)
    private Long userId;

    @Column(name = "Sub_total", nullable = false)
    private Double subTotal;

    @Column(name = "Shipping_Cost", nullable = false)
    private Double shippingCost;

    @Column(name = "Discount_Amount")
    private Double discountAmount;

    @Column(name = "Total_Amount", nullable = false)
    private Double totalAmount;

    @Column(name = "Order_Status", length = 50)
    private String orderStatus; // "PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"

    @Column(name = "Payment_Status", length = 50)
    private String paymentStatus; // "PENDING", "PAID", "COD"

    @Column(name = "Receiver_Name", length = 100)
    private String receiverName;

    @Column(name = "Shipping_Address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "Phone_Number", length = 15)
    private String phoneNumber;
}