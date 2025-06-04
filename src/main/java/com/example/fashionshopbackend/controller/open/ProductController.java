package com.example.fashionshopbackend.controller.open;

import com.example.fashionshopbackend.dto.common.PagedResponse;
import com.example.fashionshopbackend.dto.product.ProductDTO;
import com.example.fashionshopbackend.dto.product.PromotedProductDTO;
import com.example.fashionshopbackend.dto.promotion.PromotionDTO;
import com.example.fashionshopbackend.service.product.ProductService;
import com.example.fashionshopbackend.service.product.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/store")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private PromotionService promotionService;

    // Lấy danh sách khuyến mãi với danh mục
    @GetMapping("/promotions/categories")
    public List<PromotionDTO> getPromotionsByCategories() {
        return promotionService.getPromotionsByCategories   ();
    }

    // Hiển thị tất cả sản phẩm khuyến mãi
    @GetMapping("/products/promoted")
    public PagedResponse<PromotedProductDTO> getPromotedProducts(
            @RequestParam(required = false) Integer promotionId,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.getPromotedProducts(promotionId, gender, categoryId, page, size);
    }

    // Hiển thị tất cả sản phẩm (có/không khuyến mãi)
    @GetMapping("/products")
    public PagedResponse getAllProductsOrderedByLikesAndSold(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.getAllProductsOrderedByLikesAndSold(page, size);
    }

    // Hiển thị sản phẩm theo danh mục
    @GetMapping("/products/category/{categoryId}")
    public PagedResponse<ProductDTO> getProductsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.getProductsByCategory(categoryId, page, size);
    }

    // Lấy chi tiết sản phẩm
    @GetMapping("/products/{productId}")
    public ProductDTO getProductDetails(@PathVariable Integer productId) {
        return productService.getProductDetails(productId, null);
    }

    @GetMapping("/products/search")
    public PagedResponse<ProductDTO> searchProducts(
            @RequestParam String name,
            @RequestParam (defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size) {
        return productService.searchProducts(name, page, size);
    }

    @GetMapping("/products/filter")
    public PagedResponse<ProductDTO> filterProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer minReviewCount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.filterProducts(categoryId, minPrice, maxPrice, gender, minReviewCount, page, size);
    }
}