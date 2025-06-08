package com.example.fashionshopbackend.service.product;

import com.example.fashionshopbackend.dto.promotion.PromotionDTO;
import com.example.fashionshopbackend.dto.promotion.PromotedProductPromotionDTO;
import com.example.fashionshopbackend.dto.promotion.PromotionDetailsDTO;
import com.example.fashionshopbackend.dto.promotion.UpdatePromotionDTO;
import com.example.fashionshopbackend.entity.promotion.Promotion;
import com.example.fashionshopbackend.entity.promotion.PromotionCategory;
import com.example.fashionshopbackend.entity.promotion.PromotionProduct;
import com.example.fashionshopbackend.entity.product.Product;
import com.example.fashionshopbackend.entity.product.Category;
import com.example.fashionshopbackend.repository.PromotionCategoryRepository;
import com.example.fashionshopbackend.repository.PromotionProductRepository;
import com.example.fashionshopbackend.repository.PromotionRepository;
import com.example.fashionshopbackend.repository.ProductRepository;
import com.example.fashionshopbackend.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    private static final Logger logger = LoggerFactory.getLogger(PromotionService.class);

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private PromotionCategoryRepository promotionCategoryRepository;

    @Autowired
    private PromotionProductRepository promotionProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final long CACHE_TTL_SECONDS = 300; // 5 phút

    // Lấy danh sách khuyến mãi với danh mục/sản phẩm (phân trang)
    public Page<PromotionDTO> getPromotionsByCategories(Pageable pageable) {
        String cacheKey = "promotions:categories:page=" + pageable.getPageNumber() +
                ":size=" + pageable.getPageSize() +
                ":sort=" + pageable.getSort().toString().replaceAll("[, ]", "_");

        // Lấy dữ liệu từ cache
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Page) {
            Page<?> page = (Page<?>) cached;
            Object content = page.getContent();
            if (content instanceof List<?> && !((List<?>) content).isEmpty() && ((List<?>) content).get(0) instanceof PromotionDTO) {
                try {
                    @SuppressWarnings("unchecked")
                    Page<PromotionDTO> result = (Page<PromotionDTO>) page;
                    logger.info("Retrieved promotions from cache: {}", cacheKey);
                    return result;
                } catch (ClassCastException e) {
                    logger.error("Failed to cast cached data to Page<PromotionDTO> for key: {}", cacheKey, e);
                }
            } else {
                logger.warn("Invalid cache content for key: {}", cacheKey);
            }
        }

        // Nếu không có cache hoặc cache không hợp lệ, truy vấn từ cơ sở dữ liệu
        Page<Promotion> promotions = promotionRepository.findActivePromotionsPageable(LocalDate.now(), pageable);
        Page<PromotionDTO> dtos = promotions.map(promotion -> {
            PromotionDTO dto = new PromotionDTO();
            dto.setPromotionId(promotion.getPromotionId());
            dto.setName(promotion.getName());
            dto.setDiscountPercent(promotion.getDiscountPercent());
            dto.setStartDate(promotion.getStartDate().toString());
            dto.setEndDate(promotion.getEndDate().toString());
            dto.setAppliesTo(promotion.getAppliesTo());
            List<Integer> categoryIds = promotionRepository.findCategoryIdsByPromotionId(promotion.getPromotionId());
            dto.setCategoryIds(categoryIds);
            List<Integer> productIds = promotionRepository.findProductIdsByPromotionId(promotion.getPromotionId());
            dto.setProductIds(productIds);
            return dto;
        });

        // Lưu vào cache
        redisTemplate.opsForValue().set(cacheKey, dtos, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        logger.info("Cached promotions: {}", cacheKey);
        return dtos;
    }

    // Lấy chi tiết khuyến mãi áp dụng
    public PromotionDetailsDTO getPromotionDetails(Integer productId, Integer categoryId) {
        Optional<Promotion> productPromotion = Optional.empty();
        Optional<Promotion> categoryPromotion = Optional.empty();

        if (productId != null) {
            productPromotion = promotionRepository.findActivePromotionByProductId(productId, LocalDate.now());
        }
        if (categoryId != null || (productId != null && productRepository.findById(productId).isPresent())) {
            Integer catId = categoryId != null ? categoryId : productRepository.findById(productId).get().getCategory().getCategoryId();
            categoryPromotion = promotionRepository.findActivePromotionByCategoryId(catId, LocalDate.now());
        }

        // Ưu tiên khuyến mãi PRODUCT trước CATEGORY
        Promotion selectedPromotion = productPromotion.orElse(categoryPromotion.orElse(null));
        PromotionDetailsDTO detailsDTO = new PromotionDetailsDTO();
        if (selectedPromotion != null) {
            PromotionDTO promotionDTO = convertToPromotionDTO(selectedPromotion);
            detailsDTO.setPromotion(promotionDTO);

            // Lấy sản phẩm áp dụng
            List<Integer> productIds = promotionRepository.findProductIdsByPromotionId(selectedPromotion.getPromotionId());
            List<PromotionDetailsDTO.ProductDTO> productDTOs = productIds.stream()
                    .map(id -> productRepository.findById(id).orElse(null))
                    .filter(p -> p != null)
                    .map(p -> {
                        PromotionDetailsDTO.ProductDTO dto = new PromotionDetailsDTO.ProductDTO();
                        dto.setProductId(p.getProductId());
                        dto.setName(p.getName());
                        dto.setOriginalPrice(p.getPrice());
                        BigDecimal discountedPrice = calculateDiscountedPrice(p.getPrice(), selectedPromotion);
                        dto.setDiscountedPrice(discountedPrice);
                        return dto;
                    })
                    .collect(Collectors.toList());
            detailsDTO.setProducts(productDTOs);

            // Lấy danh mục áp dụng
            List<Integer> categoryIds = promotionRepository.findCategoryIdsByPromotionId(selectedPromotion.getPromotionId());
            List<PromotionDetailsDTO.CategoryDTO> categoryDTOs = categoryIds.stream()
                    .map(id -> categoryRepository.findById(id).orElse(null))
                    .filter(c -> c != null)
                    .map(c -> {
                        PromotionDetailsDTO.CategoryDTO dto = new PromotionDetailsDTO.CategoryDTO();
                        dto.setCategoryId(c.getCategoryId());
                        dto.setName(c.getName());
                        return dto;
                    })
                    .collect(Collectors.toList());
            detailsDTO.setCategories(categoryDTOs);
        }

        return detailsDTO;
    }

    // Lấy khuyến mãi áp dụng theo sản phẩm/danh mục
    public PromotedProductPromotionDTO getApplicablePromotionDTO(Integer productId, Integer categoryId) {
        Optional<Promotion> productPromotion = Optional.empty();
        Optional<Promotion> categoryPromotion = Optional.empty();

        if (productId != null) {
            productPromotion = promotionRepository.findActivePromotionByProductId(productId, LocalDate.now());
        }
        if (categoryId != null || (productId != null && productRepository.findById(productId).isPresent())) {
            Integer catId = categoryId != null ? categoryId : productRepository.findById(productId).get().getCategory().getCategoryId();
            categoryPromotion = promotionRepository.findActivePromotionByCategoryId(catId, LocalDate.now());
        }

        // Ưu tiên khuyến mãi PRODUCT trước CATEGORY
        Promotion selectedPromotion = productPromotion.orElse(categoryPromotion.orElse(null));
        return createPromotionDTO(selectedPromotion);
    }

    // Kiểm tra khuyến mãi áp dụng cho sản phẩm
    public Optional<Promotion> getApplicablePromotion(Product product) {
        Optional<Promotion> productPromotion = promotionRepository.findActivePromotionByProductId(
                product.getProductId(), LocalDate.now());
        if (productPromotion.isPresent()) {
            return productPromotion;
        }
        return promotionRepository.findActivePromotionByCategoryId(
                product.getCategory().getCategoryId(), LocalDate.now());
    }

    // Tính giá giảm giá cho sản phẩm
    public BigDecimal calculateDiscountedPrice(BigDecimal originalPrice, Promotion promotion) {
        if (promotion == null) {
            return originalPrice;
        }
        BigDecimal discountPercent = promotion.getDiscountPercent();
        return originalPrice.multiply(BigDecimal.ONE.subtract(discountPercent.divide(BigDecimal.valueOf(100))));
    }

    // Tạo PromotedProductPromotionDTO từ Promotion
    public PromotedProductPromotionDTO createPromotionDTO(Promotion promotion) {
        if (promotion == null) {
            return null;
        }
        PromotedProductPromotionDTO dto = new PromotedProductPromotionDTO();
        dto.setPromotionId(promotion.getPromotionId());
        dto.setDiscountPercent(promotion.getDiscountPercent());
        return dto;
    }

    // Tạo khuyến mãi mới
    @Transactional
    public PromotionDTO createPromotion(UpdatePromotionDTO dto) {
        validateUpdatePromotionDTO(dto);

        Promotion promotion = new Promotion();
        promotion.setName(dto.getName());
        promotion.setDescription(dto.getDescription());
        promotion.setDiscountPercent(dto.getDiscountPercent());
        promotion.setStartDate(dto.getStartDate());
        promotion.setEndDate(dto.getEndDate());
        promotion.setAppliesTo(dto.getAppliesTo());
        promotion.setGender(dto.getGender());
        promotion.setIsActive(dto.isActive());
        promotionRepository.save(promotion);

        savePromotionLinks(promotion, dto.getCategoryIds(), dto.getProductIds());
        clearPromotionCache();

        return convertToPromotionDTO(promotion);
    }

    // Cập nhật khuyến mãi
    @Transactional
    public PromotionDTO updatePromotion(Integer promotionId, UpdatePromotionDTO dto) {
        validateUpdatePromotionDTO(dto);

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Promotion not found"));

        promotion.setName(dto.getName());
        promotion.setDescription(dto.getDescription());
        promotion.setDiscountPercent(dto.getDiscountPercent());
        promotion.setStartDate(dto.getStartDate());
        promotion.setEndDate(dto.getEndDate());
        promotion.setAppliesTo(dto.getAppliesTo());
        promotion.setGender(dto.getGender());
        promotion.setIsActive(dto.isActive());
        promotionRepository.save(promotion);

        promotionCategoryRepository.deleteByPromotionId(promotionId);
        promotionProductRepository.deleteByPromotionId(promotionId);
        savePromotionLinks(promotion, dto.getCategoryIds(), dto.getProductIds());
        clearPromotionCache();

        return convertToPromotionDTO(promotion);
    }

    // Xóa khuyến mãi (xóa mềm)
    @Transactional
    public void deletePromotion(Integer promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Promotion not found"));
        promotion.setIsActive(false);
        promotionRepository.save(promotion);
        clearPromotionCache();
        logger.info("Soft deleted promotion: {}", promotionId);
    }

    // Kích hoạt/hủy kích hoạt khuyến mãi
    @Transactional
    public PromotionDTO togglePromotionActive(Integer promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Promotion not found"));
        promotion.setIsActive(!promotion.getIsActive());
        promotionRepository.save(promotion);
        clearPromotionCache();
        logger.info("Toggled active status for promotion: {} to {}", promotionId, promotion.getIsActive());
        return convertToPromotionDTO(promotion);
    }

    // Xóa cache khuyến mãi
    public void clearPromotionCache() {
        redisTemplate.delete(redisTemplate.keys("promotions:*"));
        logger.info("Cleared promotion cache");
    }

    // Validate DTO
    private void validateUpdatePromotionDTO(UpdatePromotionDTO dto) {
        if (dto.getDiscountPercent().compareTo(BigDecimal.ZERO) < 0 ||
                dto.getDiscountPercent().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Discount percent must be between 0 and 100");
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("End date must be after startDate");
        }
        if (!List.of("CATEGORY", "PRODUCT", "ALL").contains(dto.getAppliesTo())) {
            throw new IllegalArgumentException("AppliesTo must be CATEGORY, PRODUCT, or ALL");
        }
        if (!List.of("male", "female", "unisex").contains(dto.getGender())) {
            throw new IllegalArgumentException("Gender must be male, female, or unisex");
        }
    }

    // Lưu liên kết danh mục/sản phẩm
    private void savePromotionLinks(Promotion promotion, List<Integer> categoryIds, List<Integer> productIds) {
        if ("CATEGORY".equals(promotion.getAppliesTo()) && categoryIds != null) {
            categoryIds.forEach(categoryId -> {
                PromotionCategory pc = new PromotionCategory();
                pc.setPromotionId(promotion.getPromotionId());
                pc.setCategoryId(categoryId);
                promotionCategoryRepository.save(pc);
            });
        } else if ("PRODUCT".equals(promotion.getAppliesTo()) && productIds != null) {
            productIds.forEach(productId -> {
                PromotionProduct pp = new PromotionProduct();
                pp.setPromotionId(promotion.getPromotionId());
                pp.setProductId(productId);
                promotionProductRepository.save(pp);
            });
        }
        // Nếu appliesTo=ALL, không cần lưu liên kết
    }

    // Chuyển đổi sang PromotionDTO
    private PromotionDTO convertToPromotionDTO(Promotion promotion) {
        PromotionDTO dto = new PromotionDTO();
        dto.setPromotionId(promotion.getPromotionId());
        dto.setName(promotion.getName());
        dto.setDiscountPercent(promotion.getDiscountPercent());
        dto.setStartDate(promotion.getStartDate().toString());
        dto.setEndDate(promotion.getEndDate().toString());
        dto.setAppliesTo(promotion.getAppliesTo());
        List<Integer> categoryIds = promotionRepository.findCategoryIdsByPromotionId(promotion.getPromotionId());
        dto.setCategoryIds(categoryIds);
        List<Integer> productIds = promotionRepository.findProductIdsByPromotionId(promotion.getPromotionId());
        dto.setProductIds(productIds);
        return dto;
    }
}