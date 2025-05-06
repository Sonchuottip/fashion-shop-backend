package com.example.fashionshopbackend.repository.product;

import com.example.fashionshopbackend.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Tìm kiếm sản phẩm theo tên (case-insensitive)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Tìm sản phẩm theo CategoryID
    List<Product> findByCategoryId(Integer categoryId);
}