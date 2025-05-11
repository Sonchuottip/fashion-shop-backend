package com.example.fashionshopbackend.service.admin;

import com.example.fashionshopbackend.dto.product.CategoryDTO;
import com.example.fashionshopbackend.entity.product.Category;
import com.example.fashionshopbackend.repository.category.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public Category createCategory(CategoryDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Category name already exists");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        // Không cần set createdAt và updatedAt vì entity tự động xử lý
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Integer id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));

        if (!category.getName().equals(dto.getName()) && categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Category name already exists");
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        // updatedAt sẽ tự động được cập nhật bởi @PreUpdate trong entity
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category not found with ID: " + id);
        }
        categoryRepository.deleteById(id);
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getId()); // Khớp với tên trường id trong entity
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}