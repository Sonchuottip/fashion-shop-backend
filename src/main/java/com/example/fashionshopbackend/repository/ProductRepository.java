package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN PromotionProduct pp ON p.productId = pp.productId " +
            "LEFT JOIN PromotionCategory pc ON p.category.categoryId = pc.categoryId " +
            "JOIN Promotion prom ON (prom.promotionId = pp.promotionId OR prom.promotionId = pc.promotionId) " +
            "WHERE prom.isActive = true " +
            "AND :currentDate BETWEEN prom.startDate AND prom.endDate " +
            "AND (:promotionId IS NULL OR prom.promotionId = :promotionId) " +
            "AND (:gender IS NULL OR p.gender = :gender) " +
            "AND (:categoryId IS NULL OR p.category.categoryId = :categoryId)")
    Page<Product> findPromotedProducts(
            @Param("promotionId") Integer promotionId,
            @Param("gender") String gender,
            @Param("categoryId") Integer categoryId,
            @Param("currentDate") LocalDate currentDate,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status IN :statuses")
    Page<Product> findAllByStatuses(List<String> statuses, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'deleted'")
    Page<Product> findAllDeleted(Pageable pageable);

    @Modifying
    @Query("UPDATE Product p SET p.status = 'deleted' WHERE p.productId = :productId")
    void softDeleteById(Integer productId);

    @Modifying
    @Query("UPDATE Product p SET p.status = :status WHERE p.category.categoryId = :categoryId")
    void updateStatusByCategoryId(@Param("categoryId") Integer categoryId, @Param("status") String status);

    @Query("SELECT p FROM Product p WHERE p.status = 'active' AND p.category.categoryId IN :categoryIds")
    Page<Product> findAllByCategoryIdIn(List<Integer> categoryIds, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'active'")
    Page<Product> findAllActive(Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN p.sales s LEFT JOIN p.likes l " +
            "WHERE p.status = 'active' " +
            "ORDER BY COALESCE(l.totalLikes, 0) DESC, COALESCE(s.totalSold, 0) DESC")
    Page<Product> findAllActiveOrderedByLikesAndSold(Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "WHERE p.status = 'active' " +
            "AND (:categoryId IS NULL OR p.category.categoryId = :categoryId) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:gender IS NULL OR p.gender = :gender) " +
            "AND (:minRate IS NULL OR p.averageRating >= :minRate)")
    Page<Product> findAllActiveFiltered(List<Integer> categoryIds, BigDecimal minPrice, BigDecimal maxPrice,
                                        String gender, Integer minRate, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'active' AND p.name LIKE %:name%")
    Page<Product> findAllActiveByNameContaining(String name, Pageable pageable);
}