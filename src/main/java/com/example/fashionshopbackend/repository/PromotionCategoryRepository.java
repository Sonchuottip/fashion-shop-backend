package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.promotion.PromotionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionCategoryRepository extends JpaRepository<PromotionCategory, Long> {

    @Modifying
    @Query("DELETE FROM PromotionCategory pc WHERE pc.promotion.promotionId = :promotionId")
    void deleteByPromotionId(@Param("promotionId") Integer promotionId);
}