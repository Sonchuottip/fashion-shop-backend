package com.example.fashionshopbackend.service.payment;

import com.example.fashionshopbackend.dto.payment.PaymentRequest;
import com.example.fashionshopbackend.dto.payment.PaymentResponse;
import com.example.fashionshopbackend.entity.payment.Payment;
import com.example.fashionshopbackend.entity.order.Order;
import com.example.fashionshopbackend.repository.payment.PaymentRepository;
import com.example.fashionshopbackend.repository.order.OrderRepository;
import com.example.fashionshopbackend.util.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

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

    public PaymentResponse createPayment(PaymentRequest request) {
        Long userId = getCurrentUserId();
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to order");
        }

        PaymentResponse response = new PaymentResponse();
        if ("ZALOPAY".equalsIgnoreCase(request.getPaymentMethod())) {
            response = processZaloPayPayment(order, request);
        } else if ("COD".equalsIgnoreCase(request.getPaymentMethod())) {
            response = processCODPayment(order, request);
        } else {
            throw new RuntimeException("Unsupported payment method");
        }

        return response;
    }

    private PaymentResponse processZaloPayPayment(Order order, PaymentRequest request) {
        PaymentResponse response = new PaymentResponse();
        response.setMessage("Processing ZaloPay payment");

        // Chuẩn bị dữ liệu cho API ZaloPay
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("app_id", appId);
        params.add("app_user", "user_" + order.getUserId());
        params.add("app_time", String.valueOf(System.currentTimeMillis()));
        params.add("app_trans_id", "T" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8));
        params.add("amount", String.valueOf(request.getAmount()));
        params.add("description", request.getDescription());
        params.add("bank_code", "zalopayapp");

        // Tạo chữ ký HMAC-SHA256 (cần key1 từ ZaloPay)
        String data = appId + "|" + params.getFirst("app_trans_id") + "|" + params.getFirst("app_user") + "|" + params.getFirst("amount") + "|" + params.getFirst("app_time") + "|" + params.getFirst("embed_data") + "|" + params.getFirst("item");
        String mac = generateHMAC_SHA256(data, key1); // Cần triển khai hàm này
        params.add("mac", mac);

        // Gọi API ZaloPay
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<Map> zalopayResponse = restTemplate.postForEntity(zalopayEndpoint, entity, Map.class);
        Map<String, Object> result = zalopayResponse.getBody();

        if (result != null && "1".equals(result.get("return_code"))) {
            String qrCodeUrl = (String) result.get("qr_code_url");
            String transactionId = (String) result.get("zp_trans_id");

            // Lưu thông tin thanh toán
            Payment payment = new Payment();
            payment.setOrderId(order.getOrderId());
            payment.setPaymentMethod("ZALOPAY");
            payment.setTransactionId(transactionId);
            payment.setPaymentStatus("PENDING");
            payment.setAmount(request.getAmount());
            paymentRepository.save(payment);

            order.setPaymentStatus("PENDING");
            orderRepository.save(order);

            response.setQrCodeUrl(qrCodeUrl);
            response.setTransactionId(transactionId);
            response.setStatus("PENDING");
            response.setMessage("ZaloPay QR code generated successfully");
        } else {
            response.setStatus("FAILED");
            response.setMessage("ZaloPay payment failed: " + result.get("return_message"));
        }

        return response;
    }

    private PaymentResponse processCODPayment(Order order, PaymentRequest request) {
        PaymentResponse response = new PaymentResponse();
        response.setMessage("COD payment processed");

        // Lưu thông tin thanh toán
        Payment payment = new Payment();
        payment.setOrderId(order.getOrderId());
        payment.setPaymentMethod("COD");
        payment.setPaymentStatus("PENDING");
        payment.setAmount(request.getAmount());
        paymentRepository.save(payment);

        order.setPaymentStatus("COD");
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

    // Hàm tạo HMAC-SHA256 (cần triển khai theo tài liệu ZaloPay)
    private String generateHMAC_SHA256(String data, String key) {
        // Thực hiện mã hóa HMAC-SHA256 với key1
        // Sử dụng thư viện như javax.crypto.Mac
        return ""; // Placeholder, cần triển khai
    }
}