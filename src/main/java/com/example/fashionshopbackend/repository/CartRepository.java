package com.example.fashionshopbackend.repository;


import com.example.fashionshopbackend.entity.customer.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserIdAndVariantId(Long userId, Long variantId);

    Page<Cart> findByUserId(Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.userId = :userId")
    int deleteByUserId(Long userId);
}