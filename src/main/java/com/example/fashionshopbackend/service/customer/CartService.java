package com.example.fashionshopbackend.service.customer;

import com.example.fashionshopbackend.dto.common.PagedResponse;
import com.example.fashionshopbackend.dto.customer.CartItemDTO;
import com.example.fashionshopbackend.dto.product.ProductImageDTO;
import com.example.fashionshopbackend.dto.product.ProductVariantDTO;
import com.example.fashionshopbackend.dto.promotion.PromotionDTO;
import com.example.fashionshopbackend.entity.product.Product;
import com.example.fashionshopbackend.entity.product.ProductImage;
import com.example.fashionshopbackend.entity.product.ProductVariant;
import com.example.fashionshopbackend.entity.promotion.Promotion;
import com.example.fashionshopbackend.entity.customer.Cart;
import com.example.fashionshopbackend.repository.CartRepository;
import com.example.fashionshopbackend.repository.ProductRepository;
import com.example.fashionshopbackend.repository.ProductVariantRepository;
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
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private PromotionService promotionService;

    @Transactional
    public Cart addOrUpdateCartItem(Long userId, Long variantId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        Optional<Cart> existingCart = cartRepository.findByUserIdAndVariantId(userId, variantId);
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + quantity);
            return cartRepository.save(cart);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setVariantId(variantId);
            cart.setQuantity(quantity);
            return cartRepository.save(cart);
        }
    }

    @Transactional(readOnly = true)
    public PagedResponse<CartItemDTO> getCartByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Cart> cartPage = cartRepository.findByUserId(userId, pageable);
        List<CartItemDTO> cartItems = cartPage.getContent().stream()
                .map(this::convertToCartItemDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                cartItems,
                page,
                size,
                cartPage.getTotalElements(),
                cartPage.getTotalPages()
        );
    }

    @Transactional
    public Cart updateCartItem(Long userId, Long variantId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        Optional<Cart> existingCart = cartRepository.findByUserIdAndVariantId(userId, variantId);
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            cart.setQuantity(quantity);
            return cartRepository.save(cart);
        } else {
            throw new IllegalArgumentException("Sản phẩm không có trong giỏ hàng");
        }
    }

    @Transactional
    public void deleteCartItem(Long userId, Long variantId) {
        Optional<Cart> existingCart = cartRepository.findByUserIdAndVariantId(userId, variantId);
        if (existingCart.isPresent()) {
            cartRepository.delete(existingCart.get());
        } else {
            throw new IllegalArgumentException("Sản phẩm không có trong giỏ hàng");
        }
    }

    @Transactional
    public void deleteAllCartItems(Long userId) {
        cartRepository.deleteByUserId(userId);
    }

    private CartItemDTO convertToCartItemDTO(Cart cart) {
        CartItemDTO dto = new CartItemDTO();
        dto.setCartId(cart.getCartId());
        dto.setUserId(cart.getUserId());
        dto.setQuantity(cart.getQuantity());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());

        // Lấy thông tin biến thể sản phẩm
        Optional<ProductVariant> variantOpt = productVariantRepository.findByVariantId(cart.getVariantId());
        if (variantOpt.isPresent()) {
            ProductVariant variant = variantOpt.get();
            Product product = productRepository.findById(variant.getProduct().getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

            // Lấy thông tin sản phẩm
            dto.setProductId(Long.valueOf(product.getProductId()));
            dto.setProductName(product.getName());
            dto.setOriginalPrice(variant.getPrice());

            // Lấy ảnh chính
            product.getImages().stream()
                    .filter(ProductImage::getIsPrimary)
                    .findFirst()
                    .ifPresent(image -> dto.setPrimaryImage(convertToImageDTO(image)));

            // Lấy thông tin biến thể
            ProductVariantDTO variantDTO = new ProductVariantDTO();
            variantDTO.setVariantId(variant.getVariantId());
            variantDTO.setSku(variant.getSku());
            variantDTO.setColor(variant.getColor());
            variantDTO.setSize(variant.getSize());
            variantDTO.setOriginalPrice(variant.getPrice());
            variantDTO.setStock(variant.getStock());

            // Xử lý khuyến mãi
            Optional<Promotion> promotion = promotionService.getApplicablePromotion(product);
            if (promotion.isPresent()) {
                variantDTO.setDiscountedPrice(promotionService.calculateDiscountedPrice(variant.getPrice(), promotion.get()));
                dto.setPromotion(promotionService.createPromotionDTO(promotion.get()));
            } else {
                variantDTO.setDiscountedPrice(null);
                dto.setPromotion(null);
            }

            dto.setVariant(variantDTO);
        } else {
            throw new IllegalArgumentException("Biến thể sản phẩm không tồn tại");
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