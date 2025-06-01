package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.customer.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByUserId(Long userId);
    Optional<Cart> findByUserIdAndVariantId(Long userId, Long variantId);
    void deleteByUserId(Long userId);
}