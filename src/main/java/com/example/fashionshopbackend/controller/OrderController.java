package com.example.fashionshopbackend.controller;

import com.example.fashionshopbackend.dto.order.OrderDTO;
import com.example.fashionshopbackend.service.order.OrderService;
import com.example.fashionshopbackend.service.payment.PaymentService;
import com.example.fashionshopbackend.service.shipping.GHTKShippingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private GHTKShippingService ghtkShippingService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @RequestBody OrderDTO orderDTO,
            @RequestParam(required = false) String couponCode,
            @RequestParam String paymentMethod,
            @RequestParam String ipAddress,
            @RequestParam String pickProvince,
            @RequestParam String pickDistrict,
            @RequestParam String pickAddress,
            @RequestParam String pickTel) {
        try {
            OrderDTO result = orderService.createOrder(orderDTO, couponCode, paymentMethod, ipAddress, pickProvince, pickDistrict, pickAddress, pickTel);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error creating order: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer orderId) {
        try {
            OrderDTO order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching order {}: {}", orderId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDTO> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderDTO>> getOrdersByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDTO> orders = orderService.getOrdersByStatus(status, pageable);
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching orders by status {}: {}", status, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/date")
    public ResponseEntity<Page<OrderDTO>> getOrdersByDate(
            @RequestParam String date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDTO> orders = orderService.getOrdersByDate(localDate, pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error fetching orders by date {}: {}", date, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/month")
    public ResponseEntity<Page<OrderDTO>> getOrdersByMonthAndYear(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDTO> orders = orderService.getOrdersByMonthAndYear(year, month, pageable);
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching orders by year {} and month {}: {}", year, month, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/year")
    public ResponseEntity<Page<OrderDTO>> getOrdersByYear(
            @RequestParam int year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDTO> orders = orderService.getOrdersByYear(year, pageable);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Integer orderId) {
        try {
            OrderDTO order = orderService.cancelOrder(orderId);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            logger.error("Error cancelling order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Integer orderId, @RequestBody OrderDTO orderDTO) {
        try {
            OrderDTO updatedOrder = orderService.updateOrder(orderId, orderDTO);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalArgumentException e) {
            logger.error("Error updating order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/vnpay/callback")
    public ResponseEntity<String> handleVNPayCallback(@RequestBody Map<String, String> params) {
        try {
            paymentService.handleVNPayCallback(params);
            return ResponseEntity.ok("Callback processed successfully");
        } catch (Exception e) {
            logger.error("Error processing VNPay callback: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error processing callback");
        }
    }

    @PostMapping("/ghtk/webhook")
    public ResponseEntity<String> handleGHTKWebhook(@RequestBody Map<String, Object> payload) {
        try {
            ghtkShippingService.handleShippingWebhook(payload);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            logger.error("Error processing GHTK webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error processing webhook");
        }
    }
}