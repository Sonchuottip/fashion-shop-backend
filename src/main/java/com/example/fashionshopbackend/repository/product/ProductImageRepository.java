package com.example.fashionshopbackend.repository.product;

import com.example.fashionshopbackend.entity.product.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProductId(Integer productId);
    Optional<ProductImage> findByProductIdAndIsPrimary(Integer productId, Boolean isPrimary);
    // Xóa ảnh theo productId
    void deleteByProductId(Integer productId);
}