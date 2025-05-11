package com.example.fashionshopbackend.entity.customer;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(name = "Payment_Method", length = 50, nullable = false)
    private String paymentMethod; // "ZALOPAY" hoặc "COD"

    @Column(name = "TransactionID", length = 100)
    private String transactionId; // ID giao dịch từ ZaloPay

    @Column(name = "Payment_Status", length = 50)
    private String paymentStatus; // "PENDING", "SUCCESS", "FAILED"

    @Column(name = "Amount", nullable = false)
    private Double amount; // Số tiền thanh toán

    @Column(name = "Completed_At")
    private LocalDateTime completedAt;
}