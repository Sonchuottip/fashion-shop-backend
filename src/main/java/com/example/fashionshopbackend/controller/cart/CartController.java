package com.example.fashionshopbackend.controller.cart;

import com.example.fashionshopbackend.dto.admin.AdminResponse;
import com.example.fashionshopbackend.dto.cart.CartDTO;
import com.example.fashionshopbackend.service.cart.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("hasAuthority('Customer')")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    // [POST] /api/cart/add - Thêm sản phẩm vào giỏ hàng
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartDTO dto) {
        try {
            logger.debug("Adding to cart: VariantID {}", dto.getVariantId());
            CartDTO cartItem = cartService.addToCart(dto);
            logger.info("Added to cart successfully: CartID {}", cartItem.getCartId());
            return ResponseEntity.ok(cartItem);
        } catch (Exception e) {
            logger.error("Error adding to cart: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error adding to cart: " + e.getMessage()));
        }
    }

    // [GET] /api/cart - Lấy giỏ hàng của user
    @GetMapping
    public ResponseEntity<?> getCart() {
        try {
            logger.debug("Fetching cart for current user");
            List<CartDTO> cartItems = cartService.getCart();
            return ResponseEntity.ok(cartItems);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching cart: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching cart: " + e.getMessage()));
        }
    }

    // [DELETE] /api/cart/remove/{id} - Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long id) {
        try {
            logger.debug("Removing from cart: CartID {}", id);
            cartService.removeFromCart(id);
            logger.info("Removed from cart successfully: CartID {}", id);
            return ResponseEntity.ok(new AdminResponse("Removed from cart successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error removing from cart: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error removing from cart: " + e.getMessage()));
        }
    }

    // [DELETE] /api/cart/clear - Xóa toàn bộ giỏ hàng
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart() {
        try {
            logger.debug("Clearing cart for current user");
            cartService.clearCart();
            logger.info("Cart cleared successfully");
            return ResponseEntity.ok(new AdminResponse("Cart cleared successfully"));
        } catch (Exception e) {
            logger.error("Error clearing cart: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error clearing cart: " + e.getMessage()));
        }
    }
}