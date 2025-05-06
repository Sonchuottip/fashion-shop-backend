package com.example.fashionshopbackend.service.cart;

import com.example.fashionshopbackend.dto.cart.CartDTO;
import com.example.fashionshopbackend.entity.cart.Cart;
import com.example.fashionshopbackend.repository.cart.CartRepository;
import com.example.fashionshopbackend.util.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private JWTUtil jwtUtil;

    public CartDTO addToCart(CartDTO dto) {
        Long userId = getCurrentUserId();
        dto.setUserId(userId);
        Cart cart = convertToEntity(dto);
        cart = cartRepository.save(cart);
        return convertToDTO(cart);
    }

    public List<CartDTO> getCart() {
        Long userId = getCurrentUserId();
        return cartRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void removeFromCart(Long cartId) {
        Long userId = getCurrentUserId();
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found with ID: " + cartId));
        if (!cart.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to remove this cart item");
        }
        cartRepository.deleteById(cartId);
    }

    public void clearCart() {
        Long userId = getCurrentUserId();
        cartRepository.deleteByUserId(userId);
    }

    private Long getCurrentUserId() {
        String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid token: " + e.getMessage());
        }
    }

    private CartDTO convertToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setCartId(cart.getCartId());
        dto.setUserId(cart.getUserId());
        dto.setVariantId(cart.getVariantId());
        dto.setQuantity(cart.getQuantity());
        return dto;
    }

    private Cart convertToEntity(CartDTO dto) {
        Cart cart = new Cart();
        cart.setCartId(dto.getCartId());
        cart.setUserId(dto.getUserId());
        cart.setVariantId(dto.getVariantId());
        cart.setQuantity(dto.getQuantity());
        return cart;
    }
}