package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.product.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findByVariantId(Long variantId);
    Optional<ProductVariant> findBySku(String sku);
}