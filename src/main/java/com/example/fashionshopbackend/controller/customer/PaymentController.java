package com.example.fashionshopbackend.controller.customer;

import com.example.fashionshopbackend.dto.admin.AdminResponse;
import com.example.fashionshopbackend.dto.customer.PaymentRequest;
import com.example.fashionshopbackend.dto.customer.PaymentResponse;
import com.example.fashionshopbackend.service.payment.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            PaymentResponse response = paymentService.createPayment(request);
            logger.info("Payment processed for order {} with method {}", request.getOrderId(), request.getPaymentMethod());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Payment failed for order {}: {}", request.getOrderId(), e.getMessage());
            return ResponseEntity.badRequest().body(new PaymentResponse("Payment failed: " + e.getMessage(), null, null, "FAILED"));
        }
    }

    @GetMapping("/payments/order/{orderId}")
    public ResponseEntity<?> getPaymentByOrderId(@PathVariable Long orderId) {
        try {
            logger.debug("Fetching payment for order ID: {}", orderId);
            PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            logger.error("Error fetching payment for order ID {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching payment: " + e.getMessage()));
        }
    }
}