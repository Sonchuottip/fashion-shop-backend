package com.example.fashionshopbackend.repository.product;

import com.example.fashionshopbackend.entity.product.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProductId(Integer productId);

    // Xóa ảnh theo productId
    void deleteByProductId(Integer productId);
}