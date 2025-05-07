package com.example.fashionshopbackend.service.wishlist;

import com.example.fashionshopbackend.dto.wishlist.WishListDTO;
import com.example.fashionshopbackend.entity.wishlist.WishList;
import com.example.fashionshopbackend.repository.wishlist.WishListRepository;
import com.example.fashionshopbackend.util.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishListService {

    @Autowired
    private WishListRepository wishListRepository;

    @Autowired
    private JWTUtil jwtUtil;

    public List<WishListDTO> getWishList() {
        Long userId = getCurrentUserId();
        return wishListRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public WishListDTO addToWishList(Long productId) {
        Long userId = getCurrentUserId();
        if (wishListRepository.findByUserIdAndProductId(userId, productId).isPresent()) {
            throw new IllegalArgumentException("Product already in wishlist");
        }

        WishList wishList = new WishList();
        wishList.setUserId(userId);
        wishList.setProductId(productId);
        wishList = wishListRepository.save(wishList);
        return convertToDTO(wishList);
    }

    public void removeFromWishList(Long productId) {
        Long userId = getCurrentUserId();
        if (wishListRepository.findByUserIdAndProductId(userId, productId).isEmpty()) {
            throw new IllegalArgumentException("Product not found in wishlist");
        }
        wishListRepository.deleteByUserIdAndProductId(userId, productId);
    }

    private Long getCurrentUserId() {
        String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid token: " + e.getMessage());
        }
    }

    private WishListDTO convertToDTO(WishList wishList) {
        WishListDTO dto = new WishListDTO();
        dto.setWishListId(wishList.getWishListId());
        dto.setUserId(wishList.getUserId());
        dto.setProductId(wishList.getProductId());
        dto.setCreatedAt(wishList.getCreatedAt() != null ?
                wishList.getCreatedAt().toString() : null);
        return dto;
    }
}