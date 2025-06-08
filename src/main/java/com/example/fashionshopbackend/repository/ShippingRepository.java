package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.shipping.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, Integer> {
    Optional<Shipping> findByOrderId(Integer orderId);
    Optional<Shipping> findByTrackingNumber(String trackingNumber);
}