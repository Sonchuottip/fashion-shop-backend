package com.example.fashionshopbackend.controller.admin;

import com.example.fashionshopbackend.dto.admin.AdminResponse;
import com.example.fashionshopbackend.dto.category.CategoryDTO;
import com.example.fashionshopbackend.dto.coupon.CouponDTO;
import com.example.fashionshopbackend.dto.product.ProductWithImagesAndVariantsDTO;
import com.example.fashionshopbackend.dto.user.UserDTO;
import com.example.fashionshopbackend.service.admin.CategoryService;
import com.example.fashionshopbackend.service.admin.CouponService;
import com.example.fashionshopbackend.service.admin.ProductService;

import com.example.fashionshopbackend.service.admin.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('Admin')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        try {
            logger.debug("Fetching all categories");
            List<CategoryDTO> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Error fetching categories: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching categories: " + e.getMessage()));
        }
    }

    // Categories - Thêm, sửa, xóa danh mục sản phẩm
    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryDTO dto) {
        try {
            logger.debug("Creating category: {}", dto.getName());
            categoryService.createCategory(dto);
            logger.info("Category created successfully: {}", dto.getName());
            return ResponseEntity.ok(new AdminResponse("Category created successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Error creating category: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating category: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error creating category: " + e.getMessage()));
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @Valid @RequestBody CategoryDTO dto) {
        try {
            logger.debug("Updating category ID: {}", id);
            categoryService.updateCategory(id, dto);
            logger.info("Category updated successfully: ID {}", id);
            return ResponseEntity.ok(new AdminResponse("Category updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Category not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating category: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error updating category: " + e.getMessage()));
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        try {
            logger.debug("Deleting category ID: {}", id);
            categoryService.deleteCategory(id);
            logger.info("Category deleted successfully: ID {}", id);
            return ResponseEntity.ok(new AdminResponse("Category deleted successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Category not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deleting category: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error deleting category: " + e.getMessage()));
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts() {
        try {
            logger.debug("Fetching all products with images and variants");
            return ResponseEntity.ok(productService.getAllProductsWithImagesAndVariants());
        } catch (Exception e) {
            logger.error("Error fetching products: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching products: " + e.getMessage()));
        }
    }

    // Tạo mới sản phẩm cùng ảnh và biến thể
    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductWithImagesAndVariantsDTO dto) {
        try {
            logger.debug("Creating product with images and variants: {}", dto.getName());
            productService.createProductWithImagesAndVariants(dto);
            logger.info("Product, images, and variants created successfully: {}", dto.getName());
            return ResponseEntity.ok(new AdminResponse("Product, images, and variants created successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating product with images and variants: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error creating product with images and variants: " + e.getMessage()));
        }
    }

    // Cập nhật sản phẩm cùng ảnh và biến thể
    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer id, @Valid @RequestBody ProductWithImagesAndVariantsDTO dto) {
        try {
            logger.debug("Updating product ID: {} with images and variants", id);
            dto.setProductId(id); // Đảm bảo productId khớp với đường dẫn
            productService.updateProductWithImagesAndVariants(dto);
            logger.info("Product, images, and variants updated successfully: ID {}", id);
            return ResponseEntity.ok(new AdminResponse("Product, images, and variants updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Product not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating product with images and variants: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error updating product with images and variants: " + e.getMessage()));
        }
    }

    // Xóa sản phẩm cùng ảnh và biến thể
    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        try {
            logger.debug("Deleting product ID: {} with images and variants", id);
            productService.deleteProductWithImagesAndVariants(id);
            logger.info("Product, images, and variants deleted successfully: ID {}", id);
            return ResponseEntity.ok(new AdminResponse("Product, images, and variants deleted successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Product not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deleting product with images and variants: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error deleting product with images and variants: " + e.getMessage()));
        }
    }

    @PostMapping("/coupons")
    public ResponseEntity<?> createCoupon(@Valid @RequestBody CouponDTO dto) {
        try {
            logger.debug("Creating coupon: {}", dto.getCode());
            couponService.createCoupon(dto);
            logger.info("Coupon created successfully: {}", dto.getCode());
            return ResponseEntity.ok(new AdminResponse("Coupon created successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Error creating coupon: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating coupon: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error creating coupon: " + e.getMessage()));
        }
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<?> updateCoupon(@PathVariable Integer id, @Valid @RequestBody CouponDTO dto) {
        try {
            logger.debug("Updating coupon ID: {}", id);
            dto.setCouponId(id); // Đảm bảo couponId khớp với đường dẫn
            couponService.updateCoupon(dto);
            logger.info("Coupon updated successfully: ID {}", id);
            return ResponseEntity.ok(new AdminResponse("Coupon updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Coupon not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating coupon: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error updating coupon: " + e.getMessage()));
        }
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Integer id) {
        try {
            logger.debug("Deleting coupon ID: {}", id);
            couponService.deleteCoupon(id);
            logger.info("Coupon deleted successfully: ID {}", id);
            return ResponseEntity.ok(new AdminResponse("Coupon deleted successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Coupon not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deleting coupon: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error deleting coupon: " + e.getMessage()));
        }
    }

    // Danh sách người dùng
    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        try {
            logger.debug("Fetching all users");
            List<UserDTO> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching users: " + e.getMessage()));
        }
    }

    // Xuất dữ liệu ra Excel
    @GetMapping("/export")
    public void exportData(HttpServletResponse response) throws IOException {
        try {
            logger.debug("Exporting data to Excel");
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=data_export_" + System.currentTimeMillis() + ".xlsx");
            couponService.exportDataToExcel(response);
            logger.info("Data exported successfully");
        } catch (Exception e) {
            logger.error("Error exporting data: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error exporting data: " + e.getMessage());
        }
    }
}