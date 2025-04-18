package com.example.fashionshopbackend.repository.product;

import com.example.fashionshopbackend.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}