package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.order.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Integer> {

    Page<Orders> findAll(Pageable pageable);

    Page<Orders> findByOrderStatus(String orderStatus, Pageable pageable);

    @Query("SELECT o FROM Orders o WHERE DATE(o.createdAt) = :date")
    Page<Orders> findByCreatedAtDate(LocalDate date, Pageable pageable);

    @Query("SELECT o FROM Orders o WHERE YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month")
    Page<Orders> findByCreatedAtMonthAndYear(int year, int month, Pageable pageable);

    @Query("SELECT o FROM Orders o WHERE YEAR(o.createdAt) = :year")
    Page<Orders> findByCreatedAtYear(int year, Pageable pageable);
}