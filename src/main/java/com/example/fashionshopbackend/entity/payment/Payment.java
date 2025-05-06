package com.example.fashionshopbackend.entity.payment;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentID")
    private Long paymentId;

    @Column(name = "OrderID", nullable = false)
    private Long orderId;

    @Column(name = "PaymentMethod", length = 50, nullable = false)
    private String paymentMethod; // "ZALOPAY" hoặc "COD"

    @Column(name = "TransactionID", length = 100)
    private String transactionId; // ID giao dịch từ ZaloPay

    @Column(name = "PaymentStatus", length = 50)
    private String paymentStatus; // "PENDING", "SUCCESS", "FAILED"

    @Column(name = "Amount", nullable = false)
    private Double amount; // Số tiền thanh toán
}