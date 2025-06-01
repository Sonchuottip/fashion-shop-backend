package com.example.fashionshopbackend.controller.open;

import com.example.fashionshopbackend.dto.product.CategoryDTO;
import com.example.fashionshopbackend.service.product.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store")
public class StoreCats {

    @Autowired
    private CategoryService categoryService;

    // API cho cửa hàng: Truy vấn tất cả danh mục active
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllActiveCategories() {
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }

    // API cho cửa hàng: Truy vấn tất cả danh mục cha active
    @GetMapping("/categories/parents")
    public ResponseEntity<List<CategoryDTO>> getAllActiveParentCategories() {
        return ResponseEntity.ok(categoryService.getAllActiveParentCategories());
    }

    // API cho cửa hàng: Truy vấn tất cả danh mục con active theo danh mục cha
    @GetMapping("/categories/{parentId}/subcategories")
    public ResponseEntity<List<CategoryDTO>> getAllActiveSubCategoriesByParentId(@PathVariable Integer parentId) {
        try {
            return ResponseEntity.ok(categoryService.getAllActiveSubCategoriesByParentId(parentId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

}