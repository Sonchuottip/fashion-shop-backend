package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    @Query("SELECT EXISTS (SELECT 1 FROM Category c WHERE c.name = :name AND " +
            "((:parentCategoryId IS NULL AND c.parentCategory IS NULL) OR c.parentCategory.categoryId = :parentCategoryId))")
    boolean existsByNameAndParentCategoryId(@Param("name") String name, @Param("parentCategoryId") Integer parentCategoryId);

    @Query("SELECT CASE WHEN COUNT(c) = 0 THEN true ELSE false END FROM Category c WHERE c.parentCategory.categoryId = :categoryId")
    boolean isLeafCategory(@Param("categoryId") Integer categoryId);

    @Query("SELECT EXISTS (SELECT 1 FROM Product p WHERE p.category.categoryId = :categoryId AND p.status = 'active')")
    boolean existsByCategoryIdAndProductStatus(@Param("categoryId") Integer categoryId);

    // Truy vấn tất cả danh mục theo trạng thái
    List<Category> findByStatus(String status);

    // Truy vấn tất cả danh mục cha (cho admin, bất kể trạng thái)
    List<Category> findByParentCategoryIsNull();

    // Truy vấn tất cả danh mục cha active (cho cửa hàng)
    @Query("SELECT c FROM Category c WHERE c.parentCategory IS NULL AND c.status = 'active'")
    List<Category> findActiveParentCategories();

    // Truy vấn danh mục con trực tiếp theo danh mục cha (cho admin, bất kể trạng thái)
    @Query("SELECT c FROM Category c WHERE c.parentCategory.categoryId = :parentId")
    List<Category> findDirectSubCategoriesByParentId(@Param("parentId") Integer parentId);

    // Truy vấn danh mục con trực tiếp active theo danh mục cha (cho cửa hàng)
    @Query("SELECT c FROM Category c WHERE c.parentCategory.categoryId= :parentId AND c.status = 'active'")
    List<Category> findActiveDirectSubCategoriesByParentId(@Param("parentId") Integer parentId);
}