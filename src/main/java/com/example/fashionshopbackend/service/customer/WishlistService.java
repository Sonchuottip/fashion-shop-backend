package com.example.fashionshopbackend.service.customer;

import com.example.fashionshopbackend.dto.common.PagedResponse;
import com.example.fashionshopbackend.dto.customer.WishlistItemDTO;
import com.example.fashionshopbackend.dto.product.ProductImageDTO;
import com.example.fashionshopbackend.dto.promotion.PromotedProductPromotionDTO;
import com.example.fashionshopbackend.entity.product.Product;
import com.example.fashionshopbackend.entity.product.ProductImage;
import com.example.fashionshopbackend.entity.promotion.Promotion;
import com.example.fashionshopbackend.entity.customer.Wishlist;
import com.example.fashionshopbackend.repository.ProductRepository;
import com.example.fashionshopbackend.repository.WishlistRepository;
import com.example.fashionshopbackend.service.product.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PromotionService promotionService;

    @Transactional
    public Wishlist addWishlistItem(Long userId, Integer productId) {
        Optional<Wishlist> existingWishlist = wishlistRepository.findByUserIdAndProductId(userId, productId);
        if (existingWishlist.isPresent()) {
            throw new IllegalArgumentException("Sản phẩm đã có trong danh sách yêu thích");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(userId);
        wishlist.setProductId(productId);
        return wishlistRepository.save(wishlist);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WishlistItemDTO> getWishlistByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Wishlist> wishlistPage = wishlistRepository.findByUserId(userId, pageable);
        List<WishlistItemDTO> wishlistItems = wishlistPage.getContent().stream()
                .map(this::convertToWishlistItemDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                wishlistItems,
                page,
                size,
                wishlistPage.getTotalElements(),
                wishlistPage.getTotalPages()
        );
    }

    @Transactional
    public void deleteWishlistItem(Long userId, Integer productId) {
        Optional<Wishlist> existingWishlist = wishlistRepository.findByUserIdAndProductId(userId, productId);
        if (existingWishlist.isPresent()) {
            wishlistRepository.delete(existingWishlist.get());
        } else {
            throw new IllegalArgumentException("Sản phẩm không có trong danh sách yêu thích");
        }
    }

    @Transactional
    public void deleteAllWishlistItems(Long userId) {
        wishlistRepository.deleteByUserId(userId);
    }

    private WishlistItemDTO convertToWishlistItemDTO(Wishlist wishlist) {
        WishlistItemDTO dto = new WishlistItemDTO();
        dto.setWishlistId(wishlist.getWishlistId());
        dto.setUserId(wishlist.getUserId());
        dto.setCreatedAt(wishlist.getCreatedAt());

        // Lấy thông tin sản phẩm
        Optional<Product> productOpt = productRepository.findById(wishlist.getProductId());
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            dto.setProductId(product.getProductId());
            dto.setProductName(product.getName());
            dto.setOriginalPrice(product.getPrice());

            // Lấy ảnh chính
            product.getImages().stream()
                    .filter(ProductImage::getIsPrimary)
                    .findFirst()
                    .ifPresent(image -> dto.setPrimaryImage(convertToImageDTO(image)));

            // Xử lý khuyến mãi
            Optional<Promotion> promotion = promotionService.getApplicablePromotion(product);
            if (promotion.isPresent()) {
                dto.setDiscountedPrice(promotionService.calculateDiscountedPrice(product.getPrice(), promotion.get()));
                dto.setPromotion(promotionService.createPromotionDTO(promotion.get()));
            } else {
                dto.setDiscountedPrice(null);
                dto.setPromotion(null);
            }
        } else {
            throw new IllegalArgumentException("Sản phẩm không tồn tại");
        }

        return dto;
    }

    private ProductImageDTO convertToImageDTO(ProductImage image) {
        ProductImageDTO dto = new ProductImageDTO();
        dto.setImageId(image.getImageId());
        dto.setImageUrl(image.getImageUrl());
        dto.setIsPrimary(image.getIsPrimary());
        return dto;
    }
}
