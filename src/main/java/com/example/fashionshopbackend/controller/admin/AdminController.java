package com.example.fashionshopbackend.controller.admin;

import com.example.fashionshopbackend.dto.admin.AdminResponse;
import com.example.fashionshopbackend.dto.adminlog.AdminLogDTO;
import com.example.fashionshopbackend.dto.category.CategoryDTO;
import com.example.fashionshopbackend.dto.coupon.CouponDTO;
import com.example.fashionshopbackend.dto.inventoryhistory.InventoryHistoryDTO;
import com.example.fashionshopbackend.dto.inventoryhistory.InventoryUpdateDTO;
import com.example.fashionshopbackend.dto.product.ProductDTO;
import com.example.fashionshopbackend.dto.product.ProductWithImagesAndVariantsDTO;
import com.example.fashionshopbackend.dto.user.UserDTO;
import com.example.fashionshopbackend.entity.coupon.Coupon;
import com.example.fashionshopbackend.service.admin.CategoryService;
import com.example.fashionshopbackend.service.admin.ProductService;
import com.example.fashionshopbackend.service.admin.UserService;
import com.example.fashionshopbackend.service.adminlog.AdminLogService;
import com.example.fashionshopbackend.service.coupon.CouponService;
import com.example.fashionshopbackend.service.inventoryhistory.InventoryHistoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private AdminLogService adminLogService;

    @Autowired
    private InventoryHistoryService inventoryHistoryService;

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
            logger.debug("Fetching all products");
            List<ProductDTO> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Error fetching products: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching products: " + e.getMessage()));
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProductWithImagesAndVariantsById(@PathVariable Integer id) {
        try {
            logger.debug("Fetching product with ID: {}", id);
            ProductWithImagesAndVariantsDTO product = productService.getProductWithImagesAndVariantsById(id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            logger.error("Error fetching product with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching product: " + e.getMessage()));
        }
    }

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

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer id, @Valid @RequestBody ProductWithImagesAndVariantsDTO dto) {
        try {
            logger.debug("Updating product ID: {} with images and variants", id);
            dto.setProductId(id);
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

    @GetMapping("/coupons")
    public ResponseEntity<?> getAllCoupons() {
        try {
            logger.debug("Fetching all coupons");
            List<CouponDTO> coupons = couponService.getAllCoupons();
            return ResponseEntity.ok(coupons);
        } catch (Exception e) {
            logger.error("Error fetching coupons: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching coupons: " + e.getMessage()));
        }
    }

    @PostMapping("/coupons")
    public ResponseEntity<?> createCoupon(@Valid @RequestBody CouponDTO dto) {
        try {
            logger.debug("Creating coupon: {}", dto.getCode());
            Coupon coupon = couponService.createCoupon(dto);
            logger.info("Coupon created successfully: {}", coupon.getCode());
            return ResponseEntity.ok(new AdminResponse("Coupon created successfully with ID: " + coupon.getCouponId()));
        } catch (IllegalArgumentException e) {
            logger.error("Error creating coupon: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse("Failed to create coupon: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating coupon: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error creating coupon: " + e.getMessage()));
        }
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<?> updateCoupon(@PathVariable Long id, @Valid @RequestBody CouponDTO dto) {
        try {
            logger.debug("Updating coupon ID: {}", id);
            dto.setCouponId(id);
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
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id) {
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

    @GetMapping("/logs")
    public ResponseEntity<?> getAdminLogs() {
        try {
            logger.debug("Fetching all admin logs");
            List<AdminLogDTO> logs = adminLogService.getAllAdminLogs();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            logger.error("Error fetching admin logs: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching admin logs: " + e.getMessage()));
        }
    }

    @GetMapping("/inventory/history/{variantId}")
    public ResponseEntity<?> getInventoryHistory(@PathVariable Long variantId) {
        try {
            logger.debug("Fetching inventory history for variant ID: {}", variantId);
            List<InventoryHistoryDTO> history = inventoryHistoryService.getInventoryHistoryByVariant(variantId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Error fetching inventory history: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching inventory history: " + e.getMessage()));
        }
    }

    @GetMapping("/inventory/{variantId}")
    public ResponseEntity<?> getCurrentInventory(@PathVariable Long variantId) {
        try {
            logger.debug("Fetching current inventory for variant ID: {}", variantId);
            Integer currentQuantity = inventoryHistoryService.getCurrentInventory(variantId);
            return ResponseEntity.ok(new AdminResponse("Current inventory: " + currentQuantity));
        } catch (IllegalArgumentException e) {
            logger.error("Variant not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching current inventory: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching current inventory: " + e.getMessage()));
        }
    }

    @PostMapping("/inventory/{variantId}/update")
    public ResponseEntity<?> updateInventory(@PathVariable Long variantId, @Valid @RequestBody InventoryUpdateDTO dto) {
        try {
            logger.debug("Updating inventory for variant ID: {}, quantity: {}, reason: {}", variantId, dto.getQuantity(), dto.getReason());
            inventoryHistoryService.updateInventory(variantId, dto.getQuantity(), dto.getReason());
            logger.info("Inventory updated successfully for variant ID: {}", variantId);
            return ResponseEntity.ok(new AdminResponse("Inventory updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Error updating inventory: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating inventory: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error updating inventory: " + e.getMessage()));
        }
    }
}