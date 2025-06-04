package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.promotion.PromotionProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionProductRepository extends JpaRepository<PromotionProduct, Long> {

    @Modifying
    @Query("DELETE FROM PromotionProduct pp WHERE pp.promotion.promotionId = :promotionId")
    void deleteByPromotionId(@Param("promotionId") Integer promotionId);
}