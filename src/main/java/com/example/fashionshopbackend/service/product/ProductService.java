package com.example.fashionshopbackend.service.product;

import com.example.fashionshopbackend.dto.common.PagedResponse;
import com.example.fashionshopbackend.dto.product.ProductDTO;
import com.example.fashionshopbackend.dto.product.ProductVariantDTO;
import com.example.fashionshopbackend.dto.product.PromotedProductDTO;
import com.example.fashionshopbackend.dto.product.PromotedProductVariantDTO;
import com.example.fashionshopbackend.entity.product.ProductVariant;
import com.example.fashionshopbackend.entity.promotion.Promotion;
import com.example.fashionshopbackend.entity.product.Product;
import com.example.fashionshopbackend.entity.product.ProductImage;
import com.example.fashionshopbackend.repository.CategoryRepository;
import com.example.fashionshopbackend.repository.ProductRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final long CACHE_TTL_SECONDS = 300; // 5 phút

    // Hiển thị tất cả sản phẩm khuyến mãi
    public PagedResponse<PromotedProductDTO> getPromotedProducts(Integer promotionId, String gender,
                                                                 Integer categoryId, int page, int size) {
        String cacheKey = String.format("products:promoted:page:%d:size:%d:promotionId:%s:gender:%s:categoryId:%s",
                page, size, promotionId != null ? promotionId : "null",
                gender != null ? gender : "null", categoryId != null ? categoryId : "null");
        PagedResponse<PromotedProductDTO> cachedResponse = (PagedResponse<PromotedProductDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findPromotedProducts(
                promotionId, gender, categoryId, LocalDate.now(), pageable);

        List<PromotedProductDTO> dtos = productPage.getContent().stream()
                .map(this::convertToPromotedProductDTO)
                .collect(Collectors.toList());

        PagedResponse<PromotedProductDTO> response = new PagedResponse<>(
                dtos, page, size, productPage.getTotalElements(), productPage.getTotalPages());

        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        return response;
    }

    // Hiển thị tất cả sản phẩm (có/không khuyến mãi)
    public PagedResponse<ProductDTO> getAllProductsOrderedByLikesAndSold(int page, int size) {
        String cacheKey = String.format("products:all:page:%d:size:%d", page, size);
        PagedResponse<ProductDTO> cachedResponse = (PagedResponse<ProductDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAllActiveOrderedByLikesAndSold(pageable);
        List<ProductDTO> dtos = productPage.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());

        PagedResponse<ProductDTO> response = new PagedResponse<>(
                dtos, page, size, productPage.getTotalElements(), productPage.getTotalPages());

        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        return response;
    }

    // Hiển thị sản phẩm theo danh mục
    public PagedResponse<ProductDTO> getProductsByCategory(Integer categoryId, int page, int size) {
        String cacheKey = String.format("products:category:%d:page:%d:size:%d", categoryId, page, size);
        PagedResponse<ProductDTO> cachedResponse = (PagedResponse<ProductDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        Pageable pageable = PageRequest.of(page, size);
        List<Integer> categoryIds = categoryService.findAllSubCategoryIds(categoryId);
        categoryIds.add(categoryId);

        Page<Product> productPage = productRepository.findAllByCategoryIdIn(categoryIds, pageable);
        List<ProductDTO> dtos = productPage.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());

        PagedResponse<ProductDTO> response = new PagedResponse<>(
                dtos, page, size, productPage.getTotalElements(), productPage.getTotalPages());

        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        return response;
    }

    // Lấy chi tiết sản phẩm
    @Transactional(readOnly = true)
    public ProductDTO getProductDetails(Integer productId, String statusFilter) {
        String cacheKey = String.format("product:%d", productId);
        ProductDTO cachedProduct = (ProductDTO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedProduct != null) {
            return cachedProduct;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        ProductDTO dto = convertToDTO(product);
        dto.setImages(productImageService.getAllImages(productId));
        dto.setVariants(productVariantService.getVariantsByStatus(productId, statusFilter));

        // Thêm giá sau khi giảm nếu có khuyến mãi
        Optional<Promotion> promotion = promotionService.getApplicablePromotion(product);
        if (promotion.isPresent()) {
            dto.setDiscountedPrice(promotionService.calculateDiscountedPrice(product.getPrice(), promotion.get()));
            dto.setPromotion(promotionService.createPromotionDTO(promotion.get()));
        } else {
            dto.setDiscountedPrice(null);
            dto.setPromotion(null);
        }

        redisTemplate.opsForValue().set(cacheKey, dto, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        return dto;
    }

    public PagedResponse<ProductDTO> searchProducts(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAllActiveByNameContaining(name, pageable);
        List<ProductDTO> dtos = productPage.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PagedResponse<>(dtos, page, size, productPage.getTotalElements(), productPage.getTotalPages());
    }

    public PagedResponse<ProductDTO> filterProducts(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                                                    String gender, Integer minReviewCount, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<Integer> categoryIds = new ArrayList<>();
        if (categoryId != null) {
            categoryIds = categoryService.findAllSubCategoryIds(categoryId);
            categoryIds.add(categoryId);
        }

        Page<Product> productPage = productRepository.findAllActiveFiltered(
                categoryIds.isEmpty() ? null : categoryIds,
                minPrice,
                maxPrice,
                gender,
                minReviewCount,
                pageable
        );

        List<ProductDTO> dtos = productPage.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PagedResponse<>(dtos, page, size, productPage.getTotalElements(), productPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductDTO> getAllProductsForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<String> statuses = List.of("active", "inactive");
        Page<Product> productPage = productRepository.findAllByStatuses(statuses, pageable);
        List<ProductDTO> dtos = productPage.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PagedResponse<>(dtos, page, size, productPage.getTotalElements(), productPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductDTO> getDeletedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAllDeleted(pageable);
        List<ProductDTO> dtos = productPage.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PagedResponse<>(dtos, page, size, productPage.getTotalElements(), productPage.getTotalPages());
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, List<MultipartFile> images, List<ProductVariantDTO> variants) throws IOException {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getOriginalPrice());
        product.setCategory(categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại")));
        product.setGender(productDTO.getGender());
        product.setAverageRating(BigDecimal.valueOf(0));
        product.setStatus("inactive");
        product.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        product.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Product savedProduct = productRepository.save(product);

        if (images != null && !images.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                try {
                    productImageService.addProductImages(savedProduct.getProductId(), images);
                } catch (IOException e) {
                    System.err.println("Failed to upload images for product " + savedProduct.getProductId() + ": " + e.getMessage());
                }
            });
        }
        if (variants != null && !variants.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                productVariantService.addProductVariants(savedProduct.getProductId(), variants);
            });
        }

        clearProductCache(savedProduct.getProductId());
        return getProductDetails(savedProduct.getProductId(), "all");
    }

    @Transactional
    public ProductDTO updateProductDetails(Integer productId, ProductDTO productDTO) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getOriginalPrice());
        product.setCategory(categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại")));
        product.setGender(productDTO.getGender());
        product.setStatus(productDTO.getStatus());
        product.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        productRepository.save(product);

        clearProductCache(productId);
        return convertToDTO(product);
    }

    @Transactional
    public void deleteProduct(Integer productId) {
        productRepository.softDeleteById(productId);
        clearProductCache(productId);
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setOriginalPrice(product.getPrice());
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null);
        dto.setGender(product.getGender());
        dto.setAverageRating(product.getAverageRating());
        dto.setStatus(product.getStatus());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        product.getImages().stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .ifPresent(image -> dto.setPrimaryImage(productImageService.convertToImageDTO(image)));

        dto.setTotalSold(product.getSales() != null ? product.getSales().getTotalSold() : 0);
        dto.setTotalLikes(product.getLikes() != null ? product.getLikes().getTotalLikes() : 0);

        // Xử lý khuyến mãi
        Optional<Promotion> promotion = promotionService.getApplicablePromotion(product);
        if (promotion.isPresent()) {
            dto.setDiscountedPrice(promotionService.calculateDiscountedPrice(product.getPrice(), promotion.get()));
            dto.setPromotion(promotionService.createPromotionDTO(promotion.get()));
        } else {
            dto.setDiscountedPrice(null);
            dto.setPromotion(null);
        }

        return dto;
    }

    private PromotedProductDTO convertToPromotedProductDTO(Product product) {
        PromotedProductDTO dto = new PromotedProductDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setOriginalPrice(product.getPrice());
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null);
        dto.setGender(product.getGender());
        dto.setAverageRating(product.getAverageRating());
        dto.setTotalSold(product.getSales() != null ? product.getSales().getTotalSold() : 0);
        dto.setTotalLikes(product.getLikes() != null ? product.getLikes().getTotalLikes() : 0);

        // Xử lý khuyến mãi
        Optional<Promotion> promotion = promotionService.getApplicablePromotion(product);
        if (promotion.isPresent()) {
            dto.setDiscountedPrice(promotionService.calculateDiscountedPrice(product.getPrice(), promotion.get()));
            dto.setPromotion(promotionService.createPromotionDTO(promotion.get()));
        } else {
            dto.setDiscountedPrice(null);
            dto.setPromotion(null);
        }

        return dto;
    }

    private PromotedProductVariantDTO mapToPromotedProductVariantDTO(ProductVariant variant, Promotion promotion) {
        PromotedProductVariantDTO dto = new PromotedProductVariantDTO();
        dto.setVariantId(variant.getVariantId());
        dto.setSku(variant.getSku());
        dto.setColor(variant.getColor());
        dto.setSize(variant.getSize());
        dto.setOriginalPrice(variant.getPrice());
        dto.setStock(variant.getStock());

        if (promotion != null) {
            dto.setDiscountedPrice(promotionService.calculateDiscountedPrice(variant.getPrice(), promotion));
        } else {
            dto.setDiscountedPrice(null);
        }

        return dto;
    }


    private ProductVariantDTO mapToProductVariantDTO(ProductVariant variant, Promotion promotion) {
        ProductVariantDTO dto = new ProductVariantDTO();
        dto.setVariantId(variant.getVariantId());
        dto.setSku(variant.getSku());
        dto.setColor(variant.getColor());
        dto.setSize(variant.getSize());
        dto.setOriginalPrice(variant.getPrice());
        dto.setStock(variant.getStock());

        if (promotion != null) {
            dto.setDiscountedPrice(promotionService.calculateDiscountedPrice(variant.getPrice(), promotion));
        } else {
            dto.setDiscountedPrice(null);
        }

        return dto;
    }

    private void clearProductCache(Integer productId) {
        redisTemplate.delete(redisTemplate.keys("products:*"));
        redisTemplate.delete(String.format("product:%d", productId));
        promotionService.clearPromotionCache();
    }
}