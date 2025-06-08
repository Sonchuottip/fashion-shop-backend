package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.customer.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByUserIdAndProductId(Long userId, Integer productId);

    Page<Wishlist> findByUserId(Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.userId = :userId")
    int deleteByUserId(Long userId);
}