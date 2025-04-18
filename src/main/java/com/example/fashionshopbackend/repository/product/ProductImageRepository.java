package com.example.fashionshopbackend.repository.product;

import com.example.fashionshopbackend.entity.product.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
}