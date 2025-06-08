package com.example.fashionshopbackend.controller.customer;

import com.example.fashionshopbackend.dto.common.PagedResponse;
import com.example.fashionshopbackend.dto.customer.CartItemDTO;
import com.example.fashionshopbackend.dto.customer.CartRequest;
import com.example.fashionshopbackend.entity.customer.Cart;
import com.example.fashionshopbackend.service.customer.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<Cart> addOrUpdateCartItem(@RequestBody CartRequest request) {
        try {
            Cart cart = cartService.addOrUpdateCartItem(request.getUserId(), request.getVariantId(), request.getQuantity());
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<PagedResponse<CartItemDTO>> getCartByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<CartItemDTO> cartItems = cartService.getCartByUserId(userId, page, size);
        return ResponseEntity.ok(cartItems);
    }

    @PutMapping
    public ResponseEntity<Cart> updateCartItem(@RequestBody CartRequest request) {
        try {
            Cart cart = cartService.updateCartItem(request.getUserId(), request.getVariantId(), request.getQuantity());
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping
    public ResponseEntity<String> deleteCartItem(@RequestBody CartRequest request) {
        try {
            cartService.deleteCartItem(request.getUserId(), request.getVariantId());
            return ResponseEntity.ok("Xóa sản phẩm thành công");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Sản phẩm không có trong giỏ hàng");
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteAllCartItems(@PathVariable Long userId) {
        cartService.deleteAllCartItems(userId);
        return ResponseEntity.ok("Xóa toàn bộ giỏ hàng thành công");
    }
}
