package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.payment.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Integer> {
    Optional<Payments> findByOrderId(Integer orderId);
    Optional<Payments> findByTransactionId(String transactionId);
    long countByOrderIdAndPaymentStatus(Integer orderId, String paymentStatus);
}