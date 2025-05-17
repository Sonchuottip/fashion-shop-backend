package com.example.fashionshopbackend.service.payment;

import com.example.fashionshopbackend.dto.customer.PaymentRequest;
import com.example.fashionshopbackend.dto.customer.PaymentResponse;
import com.example.fashionshopbackend.dto.customer.PaymentUpdateRequest;
import com.example.fashionshopbackend.entity.common.Coupon;
import com.example.fashionshopbackend.entity.customer.Order;
import com.example.fashionshopbackend.entity.customer.OrderCoupon;
import com.example.fashionshopbackend.entity.customer.Payment;
import com.example.fashionshopbackend.repository.coupon.CouponRepository;
import com.example.fashionshopbackend.repository.order.OrderRepository;
import com.example.fashionshopbackend.repository.ordercoupon.OrderCouponRepository;
import com.example.fashionshopbackend.repository.payment.PaymentRepository;
import com.example.fashionshopbackend.util.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderCouponRepository orderCouponRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${zalopay.app_id}")
    private String appId;

    @Value("${zalopay.key1}")
    private String key1;

    @Value("${zalopay.endpoint}")
    private String zalopayEndpoint;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Long userId = getCurrentUserId();
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to order");
        }

        PaymentResponse response;
        if ("ZALOPAY".equalsIgnoreCase(request.getPaymentMethod())) {
            response = processZaloPayPayment(order, request);
        } else if ("COD".equalsIgnoreCase(request.getPaymentMethod())) {
            response = processCODPayment(order, request);
        } else {
            throw new RuntimeException("Unsupported payment method");
        }

        // Tăng usedCount nếu thanh toán được tạo thành công (cho cả ZaloPay và COD)
        if ("SUCCESS".equals(response.getStatus()) || "PENDING".equals(response.getStatus())) {
            orderCouponRepository.findByOrderId(order.getOrderId()).ifPresent(orderCoupon -> {
                Coupon coupon = couponRepository.findById(orderCoupon.getCouponId())
                        .orElseThrow(() -> new RuntimeException("Coupon not found: " + orderCoupon.getCouponId()));
                coupon.setUsedCount(coupon.getUsedCount() + 1);
                couponRepository.save(coupon);
            });
        }

        return response;
    }

    @Transactional
    public PaymentResponse updatePayment(Long paymentId, PaymentUpdateRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"PENDING".equals(payment.getPaymentStatus())) {
            throw new RuntimeException("Payment status cannot be updated");
        }

        payment.setPaymentStatus(request.getPaymentStatus());
        if ("Completed".equals(request.getPaymentStatus())) {
            payment.setCompletedAt(LocalDateTime.now());
            order.setPaymentStatus("Completed");
        } else if ("Failed".equals(request.getPaymentStatus()) || "Cancelled".equals(request.getPaymentStatus())) {
            order.setPaymentStatus(request.getPaymentStatus());

            // Giảm usedCount nếu thanh toán thất bại hoặc bị hủy
            orderCouponRepository.findByOrderId(order.getOrderId()).ifPresent(orderCoupon -> {
                Coupon coupon = couponRepository.findById(orderCoupon.getCouponId())
                        .orElseThrow(() -> new RuntimeException("Coupon not found: " + orderCoupon.getCouponId()));
                if (coupon.getUsedCount() > 0) {
                    coupon.setUsedCount(coupon.getUsedCount() - 1);
                    couponRepository.save(coupon);
                }
            });
        }
        paymentRepository.save(payment);
        orderRepository.save(order);

        return new PaymentResponse(
                "Payment updated to " + request.getPaymentStatus(),
                null,
                null,
                request.getPaymentStatus()
        );
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Long userId = getCurrentUserId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("admin"));

        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to order");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order ID: " + orderId));

        return new PaymentResponse(
                "Payment retrieved successfully",
                payment.getTransactionId(),
                null,
                payment.getPaymentStatus()
        );
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(payment -> new PaymentResponse(
                        "Payment retrieved successfully",
                        payment.getTransactionId(),
                        null,
                        payment.getPaymentStatus()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse processZaloPayPayment(Order order, PaymentRequest request) {
        PaymentResponse response = new PaymentResponse();
        response.setMessage("Processing ZaloPay payment");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("app_id", appId);
        params.add("app_user", "user_" + order.getUserId());
        params.add("app_time", String.valueOf(System.currentTimeMillis()));
        params.add("app_trans_id", "T" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8));
        params.add("amount", String.valueOf(request.getAmount()));
        params.add("description", request.getDescription());
        params.add("bank_code", "zalopayapp");

        String data = appId + "|" + params.getFirst("app_trans_id") + "|" + params.getFirst("app_user") + "|" + params.getFirst("amount") + "|" + params.getFirst("app_time") + "|" + params.getFirst("embed_data") + "|" + params.getFirst("item");
        String mac = generateHMAC_SHA256(data, key1);
        params.add("mac", mac);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<Map> zalopayResponse = restTemplate.postForEntity(zalopayEndpoint, entity, Map.class);
        Map<String, Object> result = zalopayResponse.getBody();

        if (result != null && "1".equals(result.get("return_code"))) {
            String qrCodeUrl = (String) result.get("qr_code_url");
            String transactionId = (String) result.get("zp_trans_id");

            Payment payment = new Payment();
            payment.setOrderId(order.getOrderId());
            payment.setPaymentMethod("Credit Card");
            payment.setTransactionId(transactionId);
            payment.setPaymentStatus("Completed");
            payment.setCompletedAt(LocalDateTime.now());
            payment.setAmount(request.getAmount());
            paymentRepository.save(payment);

            order.setPaymentStatus("Completed");
            orderRepository.save(order);

            response.setQrCodeUrl(qrCodeUrl);
            response.setTransactionId(transactionId);
            response.setStatus("SUCCESS");
            response.setMessage("ZaloPay payment completed successfully");
        } else {
            Payment payment = new Payment();
            payment.setOrderId(order.getOrderId());
            payment.setPaymentMethod("Credit Card");
            payment.setPaymentStatus("Failed");
            payment.setAmount(request.getAmount());
            paymentRepository.save(payment);

            order.setPaymentStatus("Failed");
            orderRepository.save(order);

            response.setStatus("FAILED");
            response.setMessage("ZaloPay payment failed: " + result.get("return_message"));
        }

        return response;
    }

    @Transactional
    public PaymentResponse processCODPayment(Order order, PaymentRequest request) {
        PaymentResponse response = new PaymentResponse();
        response.setMessage("COD payment processed");

        Payment payment = new Payment();
        payment.setOrderId(order.getOrderId());
        payment.setPaymentMethod("COD");
        payment.setPaymentStatus("Pending");
        payment.setAmount(request.getAmount());
        paymentRepository.save(payment);

        order.setPaymentStatus("Pending");
        orderRepository.save(order);

        response.setStatus("PENDING");
        response.setMessage("COD payment confirmed. Pay on delivery.");
        return response;
    }

    private Long getCurrentUserId() {
        String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid token: " + e.getMessage());
        }
    }

    private String generateHMAC_SHA256(String data, String key) {
        return "";
    }
}