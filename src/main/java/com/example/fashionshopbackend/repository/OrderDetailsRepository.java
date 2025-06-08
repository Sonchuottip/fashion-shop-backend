package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.order.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Integer> {
    List<OrderDetails> findByOrderId(Integer orderId);
}