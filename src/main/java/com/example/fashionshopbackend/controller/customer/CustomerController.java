package com.example.fashionshopbackend.controller.customer;

import com.example.fashionshopbackend.service.coupon.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/coupon")
@PreAuthorize("hasAuthority('Customer')")
public class CustomerController {

    @Autowired
    private CouponService couponService;

    @PostMapping("/claim/{couponId}")
    public ResponseEntity<String> claimCoupon(@PathVariable Long couponId) {
        try {
            String message = couponService.claimCoupon(couponId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}