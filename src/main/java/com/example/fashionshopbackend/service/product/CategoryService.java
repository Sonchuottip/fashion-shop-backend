package com.example.fashionshopbackend.service.product;

import com.example.fashionshopbackend.dto.product.CategoryDTO;
import com.example.fashionshopbackend.entity.product.Category;
import com.example.fashionshopbackend.entity.product.Product;
import com.example.fashionshopbackend.repository.CategoryRepository;
import com.example.fashionshopbackend.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public Category createCategory(CategoryDTO dto) {
        if (categoryRepository.existsByNameAndParentCategoryId(dto.getName(), dto.getParentCategoryId())) {
            throw new IllegalArgumentException("Danh mục với tên " + dto.getName() + " và parent_category_id " +
                    dto.getParentCategoryId() + " đã tồn tại.");
        }

        Category parentCategory = null;
        if (dto.getParentCategoryId() != null) {
            parentCategory = categoryRepository.findById(dto.getParentCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Danh mục cha không tồn tại: " + dto.getParentCategoryId()));
            if (!parentCategory.getStatus().equals("active")) {
                throw new IllegalStateException("Danh mục cha phải ở trạng thái active.");
            }
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setParentCategory(parentCategory);
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        category.setCreatedAt(OffsetDateTime.now());
        category.setUpdatedAt(OffsetDateTime.now());
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Integer id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Danh mục không tồn tại: " + id));

        if (!category.getName().equals(dto.getName()) ||
                (dto.getParentCategoryId() != null && category.getParentCategory() != null &&
                        !dto.getParentCategoryId().equals(category.getParentCategory().getCategoryId())) ||
                (dto.getParentCategoryId() == null && category.getParentCategory() != null) ||
                (dto.getParentCategoryId() != null && category.getParentCategory() == null)) {
            if (categoryRepository.existsByNameAndParentCategoryId(dto.getName(), dto.getParentCategoryId())) {
                throw new IllegalArgumentException("Danh mục với tên " + dto.getName() +
                        " và parent_category_id " + dto.getParentCategoryId() + " đã tồn tại.");
            }
        }

        Category parentCategory = null;
        if (dto.getParentCategoryId() != null) {
            parentCategory = categoryRepository.findById(dto.getParentCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Danh mục cha không tồn tại: " + dto.getParentCategoryId()));
            if (!parentCategory.getStatus().equals("active")) {
                throw new IllegalStateException("Danh mục cha phải ở trạng thái active.");
            }
        }

        if (dto.getStatus() != null && !dto.getStatus().equals(category.getStatus())) {
            if (!List.of("active", "inactive").contains(dto.getStatus())) {
                throw new IllegalArgumentException("Trạng thái không hợp lệ: " + dto.getStatus());
            }
            if (dto.getStatus().equals("active") && parentCategory != null && !parentCategory.getStatus().equals("active")) {
                throw new IllegalStateException("Không thể kích hoạt danh mục vì danh mục cha không active.");
            }
            updateCategoryStatus(category, dto.getStatus());
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setParentCategory(parentCategory);
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Danh mục không tồn tại: " + id));

        updateCategoryStatus(category, "inactive");
        categoryRepository.save(category);
    }

    @Async
    @Transactional
    protected void updateCategoryStatus(Category category, String newStatus) {
        category.setStatus(newStatus);

        // Cập nhật sản phẩm liên quan
        productRepository.updateStatusByCategoryId(category.getCategoryId(), newStatus);

        // Nếu không phải leaf category, cập nhật danh mục con
        if (!categoryRepository.isLeafCategory(category.getCategoryId())) {
            List<Integer> subCategoryIds = findAllSubCategoryIds(category.getCategoryId());
            if (!subCategoryIds.isEmpty()) {
                // Cập nhật danh mục con
                categoryRepository.findAllById(subCategoryIds).forEach(subCategory -> {
                    if (newStatus.equals("active") && subCategory.getParentCategory() != null &&
                            !subCategory.getParentCategory().getStatus().equals("active")) {
                        return; // Bỏ qua nếu danh mục cha không active
                    }
                    subCategory.setStatus(newStatus);
                    categoryRepository.save(subCategory);
                });

                // Cập nhật sản phẩm liên quan đến danh mục con
                String sql = "SELECT p FROM Product p WHERE p.category.id IN :categoryIds";
                Query query = entityManager.createQuery(sql, Product.class);
                query.setParameter("categoryIds", subCategoryIds);
                List<Product> products = query.getResultList();
                products.forEach(product -> {
                    if (newStatus.equals("active") && product.getCategory() != null &&
                            !product.getCategory().getStatus().equals("active")) {
                        return; // Bỏ qua nếu danh mục không active
                    }
                    product.setStatus(newStatus);
                    productRepository.save(product);
                });
            }
        }
    }

    public List<Integer> findAllSubCategoryIds(Integer categoryId) {
        List<Integer> subCategoryIds = new ArrayList<>();
        String sql = "WITH RECURSIVE category_tree AS (" +
                "    SELECT category_id FROM categories WHERE category_id = :categoryId" +
                "    UNION ALL" +
                "    SELECT c.category_id FROM categories c" +
                "    INNER JOIN category_tree ct ON c.parent_category_id = ct.category_id" +
                ") SELECT category_id FROM category_tree WHERE category_id != :categoryId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("categoryId", categoryId);
        List<?> results = query.getResultList();
        for (Object result : results) {
            subCategoryIds.add((Integer) result);
        }
        return subCategoryIds;
    }

    // API cho admin: Truy vấn tất cả danh mục (bất kể trạng thái)
    public List<CategoryDTO> getAllCategoriesForAdmin() {
        return categoryRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // API cho cửa hàng: Truy vấn tất cả danh mục active
    public List<CategoryDTO> getAllActiveCategories() {
        return categoryRepository.findByStatus("active")
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // API cho admin: Truy vấn tất cả danh mục cha (bất kể trạng thái)
    public List<CategoryDTO> getAllParentCategoriesForAdmin() {
        return categoryRepository.findByParentCategoryIsNull()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // API cho cửa hàng: Truy vấn tất cả danh mục cha active
    public List<CategoryDTO> getAllActiveParentCategories() {
        return categoryRepository.findActiveParentCategories()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // API cho admin: Truy vấn tất cả danh mục con theo danh mục cha (bất kể trạng thái)
    public List<CategoryDTO> getAllSubCategoriesByParentIdForAdmin(Integer parentId) {
        // Kiểm tra danh mục cha tồn tại
        categoryRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("Danh mục cha không tồn tại: " + parentId));

        List<Integer> subCategoryIds = findAllSubCategoryIds(parentId);
        if (subCategoryIds.isEmpty()) {
            return new ArrayList<>();
        }

        return categoryRepository.findAllById(subCategoryIds)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // API cho cửa hàng: Truy vấn tất cả danh mục con active theo danh mục cha
    public List<CategoryDTO> getAllActiveSubCategoriesByParentId(Integer parentId) {
        // Kiểm tra danh mục cha tồn tại
        categoryRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("Danh mục cha không tồn tại: " + parentId));

        List<Integer> subCategoryIds = findAllSubCategoryIds(parentId);
        if (subCategoryIds.isEmpty()) {
            return new ArrayList<>();
        }

        return categoryRepository.findAllById(subCategoryIds)
                .stream()
                .filter(category -> "active".equals(category.getStatus()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setParentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getCategoryId() : null);
        dto.setStatus(category.getStatus());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }
}