package com.example.fashionshopbackend.controller.admin;

import com.example.fashionshopbackend.dto.common.PagedResponse;
import com.example.fashionshopbackend.dto.product.ProductDTO;
import com.example.fashionshopbackend.dto.product.ProductImageDTO;
import com.example.fashionshopbackend.dto.product.ProductVariantDTO;
import com.example.fashionshopbackend.service.admin.AdminLogService;
import com.example.fashionshopbackend.service.product.ProductImageService;
import com.example.fashionshopbackend.service.product.ProductService;
import com.example.fashionshopbackend.service.product.ProductVariantService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminProduct {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private AdminLogService adminLogService;

    @GetMapping("/products")
    public PagedResponse<ProductDTO> getAllProductsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.getAllProductsForAdmin(page, size);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDTO> getProductDetails(
            @PathVariable Integer productId,
            @RequestParam(value = "variantStatus", required = false, defaultValue = "active") String variantStatus) {
        ProductDTO productDTO = productService.getProductDetails(productId, variantStatus);
        return ResponseEntity.ok(productDTO);
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDTO> createProduct(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam Integer categoryId,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) BigDecimal averageRating,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam("variants") String variantsJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<ProductVariantDTO> variants = mapper.readValue(
                variantsJson,
                new TypeReference<List<ProductVariantDTO>>() {}
        );
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName(name);
        productDTO.setDescription(description);
        productDTO.setOriginalPrice(price);
        productDTO.setCategoryId(categoryId);
        productDTO.setGender(gender);
        productDTO.setAverageRating(averageRating);
        ProductDTO createdProduct = productService.createProduct(productDTO, images, variants);
        adminLogService.logAdminAction("Tạo sản phẩm: " + name);
        return ResponseEntity.ok(createdProduct);
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer productId) {
        adminLogService.logAdminAction("Xóa sản phẩm ID: " + productId);
        productService.deleteProduct(productId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<ProductDTO> updateProductDetails(
            @PathVariable Integer productId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam Integer categoryId,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String status) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName(name);
        productDTO.setDescription(description);
        productDTO.setOriginalPrice(price);
        productDTO.setCategoryId(categoryId);
        productDTO.setGender(gender);
        productDTO.setStatus(status);
        adminLogService.logAdminAction("Cập nhật sản phẩm ID: " + productId + ", tên: " + name);
        return ResponseEntity.ok(productService.updateProductDetails(productId, productDTO));
    }

    @GetMapping("/products/deleted")
    public PagedResponse<ProductDTO> getDeletedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.getDeletedProducts(page, size);
    }

    @PostMapping("/products/{productId}/images")
    public ResponseEntity<List<ProductImageDTO>> addProductImages(
            @PathVariable Integer productId,
            @RequestPart("images") List<MultipartFile> newImages) throws IOException {
        adminLogService.logAdminAction("Thêm ảnh cho sản phẩm ID: " + productId);
        return ResponseEntity.ok(productImageService.addProductImages(productId, newImages));
    }

    @DeleteMapping("/products/{productId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Integer productId,
            @PathVariable Integer imageId) throws IOException {
        adminLogService.logAdminAction("Xóa ảnh ID: " + imageId + " của sản phẩm ID: " + productId);
        productImageService.deleteImage(productId, imageId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/products/{productId}/variants")
    public ResponseEntity<ProductVariantDTO> addProductVariants(
            @PathVariable Integer productId,
            @RequestPart("variants") List<ProductVariantDTO> newVariants) {
        adminLogService.logAdminAction("Thêm biến thể cho sản phẩm ID: " + productId);
        return ResponseEntity.ok(productVariantService.addProductVariants(productId, newVariants));
    }

    @DeleteMapping("/products/{productId}/variants/{variantId}")
    public ResponseEntity<Void> deleteVariant(
            @PathVariable Integer productId,
            @PathVariable Integer variantId) {
        adminLogService.logAdminAction("Xóa biến thể ID: " + variantId + " của sản phẩm ID: " + productId);
        productVariantService.deleteVariant(productId, variantId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/products/{productId}/variants/{variantId}")
    public ResponseEntity<ProductVariantDTO> updateProductVariant(
            @PathVariable Integer productId,
            @PathVariable Integer variantId,
            @RequestBody ProductVariantDTO variantDTO) {
        adminLogService.logAdminAction("Cập nhật biến thể ID: " + variantId + " của sản phẩm ID: " + productId);
        return ResponseEntity.ok(productVariantService.updateProductVariant(productId, variantId, variantDTO));
    }
}