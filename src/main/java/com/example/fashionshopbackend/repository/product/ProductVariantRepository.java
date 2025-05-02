package com.example.fashionshopbackend.repository.product;

import com.example.fashionshopbackend.entity.product.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {
    List<ProductVariant> findByProductId(Integer productId);

    // Xóa biến thể theo productId
    void deleteByProductId(Integer productId);
}