package com.example.fashionshopbackend.repository.category;

import com.example.fashionshopbackend.entity.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsByName(String name);
}