package com.example.fashionshopbackend.controller.admin;

import com.example.fashionshopbackend.dto.product.CategoryDTO;
import com.example.fashionshopbackend.entity.product.Category;
import com.example.fashionshopbackend.service.admin.AdminLogService;
import com.example.fashionshopbackend.service.product.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminCats {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AdminLogService adminLogService;

    @PostMapping("/categories")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO dto) {
        try {
            Category category = categoryService.createCategory(dto);
            CategoryDTO createdCategory = categoryService.convertToDTO(category);
            // Ghi log chi tiết
            adminLogService.logAdminAction("Tạo danh mục: " + createdCategory.getName());
            return ResponseEntity.ok(createdCategory);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Integer id, @RequestBody CategoryDTO dto) {
        try {
            Category category = categoryService.updateCategory(id, dto);
            CategoryDTO updatedCategory = categoryService.convertToDTO(category);
            // Ghi log chi tiết
            adminLogService.logAdminAction("Cập nhật danh mục ID: " + id + ", Tên: " + updatedCategory.getName());
            return ResponseEntity.ok(updatedCategory);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Integer id) {
        try {
            categoryService.deleteCategory(id);
            // Ghi log chi tiết
            adminLogService.logAdminAction("Xóa danh mục ID: " + id);
            return ResponseEntity.ok("Đánh dấu danh mục là inactive thành công.");
        } catch (IllegalStateException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesForAdmin() {
        return ResponseEntity.ok(categoryService.getAllCategoriesForAdmin());
    }

    @GetMapping("/categories/parents")
    public ResponseEntity<List<CategoryDTO>> getAllParentCategoriesForAdmin() {
        return ResponseEntity.ok(categoryService.getAllParentCategoriesForAdmin());
    }

    @GetMapping("/categories/{parentId}/subcategories")
    public ResponseEntity<List<CategoryDTO>> getAllSubCategoriesByParentIdForAdmin(@PathVariable Integer parentId) {
        try {
            return ResponseEntity.ok(categoryService.getAllSubCategoriesByParentIdForAdmin(parentId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}