package com.example.fashionshopbackend.controller.customer;

import com.example.fashionshopbackend.dto.admin.AdminResponse;
import com.example.fashionshopbackend.dto.customer.OrderDTO;
import com.example.fashionshopbackend.service.order.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasAuthority('Customer')")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    // [POST] /api/orders - Đặt hàng
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderDTO dto) {
        try {
            logger.debug("Creating order for current user");
            OrderDTO order = orderService.createOrder(dto);
            logger.info("Order created successfully: OrderID {}", order.getOrderId());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            logger.error("Error creating order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error creating order: " + e.getMessage()));
        }
    }

    // [GET] /api/orders/{id} - Xem chi tiết đơn hàng
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            logger.debug("Fetching order with ID: {}", id);
            OrderDTO order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            logger.error("Order not found or unauthorized: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching order: " + e.getMessage()));
        }
    }

    // [GET] /api/orders/user - Lịch sử đơn hàng
    @GetMapping("/user")
    public ResponseEntity<?> getOrdersByUser() {
        try {
            logger.debug("Fetching orders for current user");
            List<OrderDTO> orders = orderService.getOrdersByUser();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error fetching orders: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error fetching orders: " + e.getMessage()));
        }
    }

    // [POST] /api/orders/{id}/cancel - Hủy đơn hàng
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            logger.debug("Cancelling order with ID: {}", id);
            orderService.cancelOrder(id);
            logger.info("Order cancelled successfully: OrderID {}", id);
            return ResponseEntity.ok(new AdminResponse("Order cancelled successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Order not found or unauthorized: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AdminResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error cancelling order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new AdminResponse("Error cancelling order: " + e.getMessage()));
        }
    }
}