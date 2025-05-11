package com.example.fashionshopbackend.controller.customer;

import com.example.fashionshopbackend.dto.customer.WishListDTO;
import com.example.fashionshopbackend.service.wishlist.WishListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/wishlist")
@PreAuthorize("hasAuthority('Customer')")
public class WishListController {

    private static final Logger logger = LoggerFactory.getLogger(WishListController.class);

    @Autowired
    private WishListService wishListService;

    @GetMapping
    public ResponseEntity<List<WishListDTO>> getWishList() {
        try {
            List<WishListDTO> wishList = wishListService.getWishList();
            logger.info("Retrieved wishlist for user");
            return ResponseEntity.ok(wishList);
        } catch (Exception e) {
            logger.error("Failed to retrieve wishlist: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<WishListDTO> addToWishList(@RequestParam Long productId) {
        try {
            WishListDTO wishListDTO = wishListService.addToWishList(productId);
            logger.info("Added product {} to wishlist", productId);
            return ResponseEntity.ok(wishListDTO);
        } catch (Exception e) {
            logger.error("Failed to add product to wishlist: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<String> removeFromWishList(@PathVariable Long productId) {
        try {
            wishListService.removeFromWishList(productId);
            logger.info("Removed product {} from wishlist", productId);
            return ResponseEntity.ok("Product removed from wishlist");
        } catch (Exception e) {
            logger.error("Failed to remove product from wishlist: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}