package com.example.fashionshopbackend.controller.Product;

import com.example.fashionshopbackend.dto.admin.AdminResponse;
import com.example.fashionshopbackend.dto.category.CategoryDTO;
import com.example.fashionshopbackend.dto.product.ProductWithImagesAndVariantsDTO;
import com.example.fashionshopbackend.service.admin.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    // [GET] /api/products - Danh sách sản phẩm
    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        try {
            logger.debug("Fetching all products");
            List<ProductWithImagesAndVariantsDTO> products = productService.getAllProductsWithImagesAndVariants();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Error fetching products: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching products: " + e.getMessage()));
        }
    }

    // [GET] /api/products/{id} - Chi tiết sản phẩm
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Integer id) {
        try {
            logger.debug("Fetching product with ID: {}", id);
            ProductWithImagesAndVariantsDTO product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            logger.error("Product not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching product: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching product: " + e.getMessage()));
        }
    }

    // [GET] /api/products/search?query=abc - Tìm kiếm sản phẩm
    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam String query) {
        try {
            logger.debug("Searching products with query: {}", query);
            List<ProductWithImagesAndVariantsDTO> products = productService.searchProducts(query);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Error searching products: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error searching products: " + e.getMessage()));
        }
    }

    // [GET] /api/categories - Danh sách danh mục
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        try {
            logger.debug("Fetching all categories");
            List<CategoryDTO> categories = productService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Error fetching categories: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching categories: " + e.getMessage()));
        }
    }

    // [GET] /api/categories/{id}/products - Sản phẩm theo danh mục
    @GetMapping("/categories/{id}/products")
    public ResponseEntity<?> getProductsByCategoryId(@PathVariable Integer id) {
        try {
            logger.debug("Fetching products for category ID: {}", id);
            List<ProductWithImagesAndVariantsDTO> products = productService.getProductsByCategoryId(id);
            return ResponseEntity.ok(products);
        } catch (IllegalArgumentException e) {
            logger.error("Category not found or no products: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching products by category: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching products by category: " + e.getMessage()));
        }
    }
}