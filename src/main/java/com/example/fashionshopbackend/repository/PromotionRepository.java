package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.promotion.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    @Query("SELECT p FROM Promotion p " +
            "WHERE p.isActive = true AND :currentDate BETWEEN p.startDate AND p.endDate")
    List<Promotion> findActivePromotions(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT pc.categoryId FROM PromotionCategory pc WHERE pc.promotionId = :promotionId")
    List<Integer> findCategoryIdsByPromotionId(@Param("promotionId") Integer promotionId);

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.startDate <= :currentDate AND p.endDate >= :currentDate")
    Page<Promotion> findActivePromotionsPageable(@Param("currentDate") LocalDate currentDate, Pageable pageable);

    @Query("SELECT p FROM Promotion p " +
            "JOIN PromotionProduct pp ON p.promotionId = pp.promotionId " +
            "WHERE pp.productId = :productId AND p.isActive = true " +
            "AND :currentDate BETWEEN p.startDate AND p.endDate")
    Optional<Promotion> findActivePromotionByProductId(
            @Param("productId") Integer productId,
            @Param("currentDate") LocalDate currentDate);

    @Query("SELECT p FROM Promotion p " +
            "JOIN PromotionCategory pc ON p.promotionId = pc.promotionId " +
            "WHERE pc.categoryId = :categoryId AND p.isActive = true " +
            "AND :currentDate BETWEEN p.startDate AND p.endDate")
    Optional<Promotion> findActivePromotionByCategoryId(
            @Param("categoryId") Integer categoryId,
            @Param("currentDate") LocalDate currentDate);


}