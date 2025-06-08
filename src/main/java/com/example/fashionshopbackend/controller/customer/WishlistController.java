package com.example.fashionshopbackend.controller.customer;

import com.example.fashionshopbackend.dto.common.PagedResponse;
import com.example.fashionshopbackend.dto.customer.WishlistItemDTO;
import com.example.fashionshopbackend.dto.customer.WishlistRequest;
import com.example.fashionshopbackend.entity.customer.Wishlist;
import com.example.fashionshopbackend.service.customer.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @PostMapping
    public ResponseEntity<Wishlist> addWishlistItem(@RequestBody WishlistRequest request) {
        try {
            Wishlist wishlist = wishlistService.addWishlistItem(request.getUserId(), request.getProductId());
            return ResponseEntity.ok(wishlist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<PagedResponse<WishlistItemDTO>> getWishlistByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<WishlistItemDTO> wishlistItems = wishlistService.getWishlistByUserId(userId, page, size);
        return ResponseEntity.ok(wishlistItems);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteWishlistItem(@RequestBody WishlistRequest request) {
        try {
            wishlistService.deleteWishlistItem(request.getUserId(), request.getProductId());
            return ResponseEntity.ok("Xóa sản phẩm khỏi danh sách yêu thích thành công");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Sản phẩm không có trong danh sách yêu thích");
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteAllWishlistItems(@PathVariable Long userId) {
        wishlistService.deleteAllWishlistItems(userId);
        return ResponseEntity.ok("Xóa toàn bộ danh sách yêu thích thành công");
    }
}
