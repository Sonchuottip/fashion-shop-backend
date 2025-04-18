package com.example.fashionshopbackend.controller.admin;

import com.example.fashionshopbackend.dto.admin.AdminResponse;
import com.example.fashionshopbackend.dto.category.CategoryDTO;
import com.example.fashionshopbackend.dto.product.ProductDTO;
import com.example.fashionshopbackend.dto.product.ProductImageDTO;
import com.example.fashionshopbackend.dto.product.ProductVariantDTO;
import com.example.fashionshopbackend.dto.product.ProductWithImagesAndVariantsDTO;
import com.example.fashionshopbackend.entity.category.Category;
import com.example.fashionshopbackend.entity.product.Product;
import com.example.fashionshopbackend.entity.product.ProductImage;
import com.example.fashionshopbackend.entity.product.ProductVariant;
import com.example.fashionshopbackend.repository.category.CategoryRepository;
import com.example.fashionshopbackend.repository.product.ProductImageRepository;
import com.example.fashionshopbackend.repository.product.ProductRepository;
import com.example.fashionshopbackend.repository.product.ProductVariantRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('Admin')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    // Categories - Thêm, sửa, xóa danh mục sản phẩm
    @PostMapping("/categories")
    @Transactional
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryDTO dto) {
        try {
            logger.debug("Creating category: {}", dto.getName());
            if (categoryRepository.existsByName(dto.getName())) {
                logger.warn("Category already exists: {}", dto.getName());
                return ResponseEntity.badRequest().body(new AdminResponse("Category name already exists"));
            }

            Category category = new Category();
            category.setName(dto.getName());
            category.setDescription(dto.getDescription());
            categoryRepository.save(category);
            logger.info("Category created successfully: {}", dto.getName());
            return ResponseEntity.ok(new AdminResponse("Category created successfully"));
        } catch (Exception e) {
            logger.error("Error creating category: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error creating category: " + e.getMessage()));
        }
    }

    @PutMapping("/categories/{id}")
    @Transactional
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @Valid @RequestBody CategoryDTO dto) {
        try {
            logger.debug("Updating category ID: {}", id);
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));

            if (!category.getName().equals(dto.getName()) && categoryRepository.existsByName(dto.getName())) {
                logger.warn("Category name already exists: {}", dto.getName());
                return ResponseEntity.badRequest().body(new AdminResponse("Category name already exists"));
            }

            category.setName(dto.getName());
            category.setDescription(dto.getDescription());
            categoryRepository.save(category);
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
    @Transactional
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        try {
            logger.debug("Deleting category ID: {}", id);
            if (!categoryRepository.existsById(id)) {
                logger.warn("Category not found with ID: {}", id);
                return ResponseEntity.badRequest().body(new AdminResponse("Category not found with ID: " + id));
            }

            categoryRepository.deleteById(id);
            logger.info("Category deleted successfully: ID {}", id);
            return ResponseEntity.ok(new AdminResponse("Category deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting category: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error deleting category: " + e.getMessage()));
        }
    }

    // Products - Thêm, sửa, xóa sản phẩm
    @PostMapping("/products")
    @Transactional
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO dto) {
        try {
            logger.debug("Creating product: {}", dto.getName());
            Product product = new Product();
            product.setName(dto.getName());
            product.setDescription(dto.getDescription());
            product.setPrice(BigDecimal.valueOf(dto.getPrice()));
            product.setStock(dto.getStock());
            product.setStatus(dto.getStatus());

            if (dto.getCategoryId() != null) {
                Category category = categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + dto.getCategoryId()));
                product.setCategory(category);
            }

            productRepository.save(product);
            logger.info("Product created successfully: {}", dto.getName());
            return ResponseEntity.ok(new AdminResponse("Product created successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Category not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error creating product: " + e.getMessage()));
        }
    }

    @PutMapping("/products/{id}")
    @Transactional
    public ResponseEntity<?> updateProduct(@PathVariable Integer id, @Valid @RequestBody ProductDTO dto) {
        try {
            logger.debug("Updating product ID: {}", id);
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

            product.setName(dto.getName());
            product.setDescription(dto.getDescription());
            product.setPrice(BigDecimal.valueOf(dto.getPrice()));
            product.setStock(dto.getStock());
            product.setStatus(dto.getStatus());

            if (dto.getCategoryId() != null) {
                Category category = categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + dto.getCategoryId()));
                product.setCategory(category);
            } else {
                product.setCategory(null);
            }

            productRepository.save(product);
            logger.info("Product updated successfully: ID {}", id);
            return ResponseEntity.ok(new AdminResponse("Product updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Product or category not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating product: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error updating product: " + e.getMessage()));
        }
    }

    @DeleteMapping("/products/{id}")
    @Transactional
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        try {
            logger.debug("Deleting product ID: {}", id);
            if (!productRepository.existsById(id)) {
                logger.warn("Product not found with ID: {}", id);
                return ResponseEntity.badRequest().body(new AdminResponse("Product not found with ID: " + id));
            }

            productRepository.deleteById(id);
            logger.info("Product deleted successfully: ID {}", id);
            return ResponseEntity.ok(new AdminResponse("Product deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting product: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error deleting product: " + e.getMessage()));
        }
    }

    // ProductImages - Thêm, sửa, xóa ảnh sản phẩm
    @PostMapping("/product-images")
    @Transactional
    public ResponseEntity<?> createProductImage(@Valid @RequestBody ProductImageDTO dto) {
        try {
            logger.debug("Creating product image for product ID: {}", dto.getProductId());
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + dto.getProductId()));

            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setImageUrl(dto.getImageUrl());
            image.setIsPrimary(dto.getIsPrimary() != null ? dto.getIsPrimary() : false);
            productImageRepository.save(image);
            logger.info("Product image created successfully for product ID: {}", dto.getProductId());
            return ResponseEntity.ok(new AdminResponse("Product image created successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Product not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating product image: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error creating product image: " + e.getMessage()));
        }
    }

    @PutMapping("/product-images/{id}")
    @Transactional
    public ResponseEntity<?> updateProductImage(@PathVariable Integer id, @Valid @RequestBody ProductImageDTO dto) {
        try {
            logger.debug("Updating product image ID: {}", id);
            ProductImage image = productImageRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product image not found with ID: " + id));

            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + dto.getProductId()));

            image.setProduct(product);
            image.setImageUrl(dto.getImageUrl());
            image.setIsPrimary(dto.getIsPrimary() != null ? dto.getIsPrimary() : image.getIsPrimary());
            productImageRepository.save(image);
            logger.info("Product image updated successfully: ID {}", id);
            return ResponseEntity.ok(new AdminResponse("Product image updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Product or image not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating product image: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error updating product image: " + e.getMessage()));
        }
    }

    @DeleteMapping("/product-images/{id}")
    @Transactional
    public ResponseEntity<?> deleteProductImage(@PathVariable Integer id) {
        try {
            logger.debug("Deleting product image ID: {}", id);
            if (!productImageRepository.existsById(id)) {
                logger.warn("Product image not found with ID: {}", id);
                return ResponseEntity.badRequest().body(new AdminResponse("Product image not found with ID: " + id));
            }

            productImageRepository.deleteById(id);
            logger.info("Product image deleted successfully: ID {}", id);
            return ResponseEntity.ok(new AdminResponse("Product image deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting product image: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error deleting product image: " + e.getMessage()));
        }
    }

    // ProductVariants - Thêm, sửa, xóa biến thể sản phẩm
    @PostMapping("/product-variants")
    @Transactional
    public ResponseEntity<?> createProductVariant(@Valid @RequestBody ProductVariantDTO dto) {
        try {
            logger.debug("Creating product variant for product ID: {}", dto.getProductId());
            if (productVariantRepository.existsBySku(dto.getSku())) {
                logger.warn("SKU already exists: {}", dto.getSku());
                return ResponseEntity.badRequest().body(new AdminResponse("SKU already exists"));
            }

            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + dto.getProductId()));

            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setSku(dto.getSku());
            variant.setColor(dto.getColor());
            variant.setSize(dto.getSize());
            variant.setPrice(BigDecimal.valueOf(dto.getPrice()));
            variant.setStock(dto.getStock());
            productVariantRepository.save(variant);
            logger.info("Product variant created successfully for product ID: {}", dto.getProductId());
            return ResponseEntity.ok(new AdminResponse("Product variant created successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Product not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating product variant: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error creating product variant: " + e.getMessage()));
        }
    }

    @PutMapping("/product-variants/{id}")
    @Transactional
    public ResponseEntity<?> updateProductVariant(@PathVariable Integer id, @Valid @RequestBody ProductVariantDTO dto) {
        try {
            logger.debug("Updating product variant ID: {}", id);
            ProductVariant variant = productVariantRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product variant not found with ID: " + id));

            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + dto.getProductId()));

            if (!variant.getSku().equals(dto.getSku()) && productVariantRepository.existsBySku(dto.getSku())) {
                logger.warn("SKU already exists: {}", dto.getSku());
                return ResponseEntity.badRequest().body(new AdminResponse("SKU already exists"));
            }

            variant.setProduct(product);
            variant.setSku(dto.getSku());
            variant.setColor(dto.getColor());
            variant.setSize(dto.getSize());
            variant.setPrice(BigDecimal.valueOf(dto.getPrice()));
            variant.setStock(dto.getStock());
            productVariantRepository.save(variant);
            logger.info("Product variant updated successfully: ID {}", id);
            return ResponseEntity.ok(new AdminResponse("Product variant updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Product or variant not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating product variant: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error updating product variant: " + e.getMessage()));
        }
    }

    @DeleteMapping("/product-variants/{id}")
    @Transactional
    public ResponseEntity<?> deleteProductVariant(@PathVariable Integer id) {
        try {
            logger.debug("Deleting product variant ID: {}", id);
            if (!productVariantRepository.existsById(id)) {
                logger.warn("Product variant not found with ID: {}", id);
                return ResponseEntity.badRequest().body(new AdminResponse("Product variant not found with ID: " + id));
            }

            productVariantRepository.deleteById(id);
            logger.info("Product variant deleted successfully: ID {}", id);
            return ResponseEntity.ok(new AdminResponse("Product variant deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting product variant: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error deleting product variant: " + e.getMessage()));
        }
    }

    // Thêm sản phẩm cùng lúc với nhiều ảnh và nhiều biến thể
    @PostMapping("/products-with-images-and-variants")
    @Transactional
    public ResponseEntity<?> createProductWithImagesAndVariants(@Valid @RequestBody ProductWithImagesAndVariantsDTO dto) {
        try {
            logger.debug("Creating product with images and variants: {}", dto.getName());

            // Tạo sản phẩm
            Product product = new Product();
            product.setName(dto.getName());
            product.setDescription(dto.getDescription());
            product.setPrice(BigDecimal.valueOf(dto.getPrice()));
            product.setStock(dto.getStock());
            product.setStatus(dto.getStatus());

            if (dto.getCategoryId() != null) {
                Category category = categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + dto.getCategoryId()));
                product.setCategory(category);
            }

            product = productRepository.save(product);
            logger.info("Product created successfully: {}", dto.getName());

            // Tạo các ảnh
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                for (ProductImageDTO imageDTO : dto.getImages()) {
                    ProductImage image = new ProductImage();
                    image.setProduct(product);
                    image.setImageUrl(imageDTO.getImageUrl());
                    image.setIsPrimary(imageDTO.getIsPrimary() != null ? imageDTO.getIsPrimary() : false);
                    productImageRepository.save(image);
                }
                logger.info("Images created successfully for product ID: {}", product.getId());
            }

            // Tạo các biến thể
            if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
                for (ProductVariantDTO variantDTO : dto.getVariants()) {
                    if (productVariantRepository.existsBySku(variantDTO.getSku())) {
                        logger.warn("SKU already exists: {}", variantDTO.getSku());
                        throw new IllegalArgumentException("SKU already exists: " + variantDTO.getSku());
                    }

                    ProductVariant variant = new ProductVariant();
                    variant.setProduct(product);
                    variant.setSku(variantDTO.getSku());
                    variant.setColor(variantDTO.getColor());
                    variant.setSize(variantDTO.getSize());
                    variant.setPrice(BigDecimal.valueOf(variantDTO.getPrice()));
                    variant.setStock(variantDTO.getStock());
                    productVariantRepository.save(variant);
                }
                logger.info("Variants created successfully for product ID: {}", product.getId());
            }

            return ResponseEntity.ok(new AdminResponse("Product, images, and variants created successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating product with images and variants: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error creating product with images and variants: " + e.getMessage()));
        }
    }
}