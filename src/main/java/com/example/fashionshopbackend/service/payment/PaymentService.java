package com.example.fashionshopbackend.service.payment;

import com.example.fashionshopbackend.entity.order.Orders;
import com.example.fashionshopbackend.entity.payment.Payments;
import com.example.fashionshopbackend.repository.OrderRepository;
import com.example.fashionshopbackend.repository.PaymentsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnp_HashSecret;

    @Value("${vnpay.url}")
    private String vnp_Url;

    @Value("${vnpay.return-url}")
    private String vnp_ReturnUrl;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentsRepository paymentsRepository;

    @Transactional
    public String initiatePayment(Integer orderId, String paymentMethod, String ipAddress) throws Exception {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Create payment record
        Payments payment = new Payments();
        payment.setOrderId(orderId);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus("pending");
        paymentsRepository.save(payment);

        if ("vnpay".equalsIgnoreCase(paymentMethod)) {
            return createVNPayPaymentUrl(order, ipAddress, payment);
        } else if ("cod".equalsIgnoreCase(paymentMethod)) {
            // For COD, no payment URL is needed, move to processing
            order.setPaymentStatus("pending");
            order.setOrderStatus("processing");
            orderRepository.save(order);
            logger.info("Initiated COD payment for order: {}", orderId);
            return null; // No redirect URL for COD
        } else {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
    }

    private String createVNPayPaymentUrl(Orders order, String ipAddress, Payments payment) throws Exception {
        // Prepare VNPay parameters
        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf((int) (order.getTotalAmount().doubleValue() * 100)));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", String.valueOf(order.getOrderId()));
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderId());
        vnp_Params.put("vnp_OrderType", "250000");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", ipAddress);
        vnp_Params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        // Create secure hash
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            hashData.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).append("&");
        }
        hashData.setLength(hashData.length() - 1);
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        vnp_Params.put("vnp_SecureHash", vnp_SecureHash);

        // Build payment URL
        StringBuilder query = new StringBuilder(vnp_Url).append("?");
        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            query.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).append("&");
        }
        query.setLength(query.length() - 1);
        logger.info("Created VNPay payment URL for order: {}", order.getOrderId());
        return query.toString();
    }

    @Transactional
    public void handleVNPayCallback(Map<String, String> params) {
        String vnp_TxnRef = params.get("vnp_TxnRef");
        String vnp_TransactionStatus = params.get("vnp_TransactionStatus");
        String vnp_SecureHash = params.get("vnp_SecureHash");

        // Verify secure hash
        Map<String, String> fields = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!entry.getKey().equals("vnp_SecureHash")) {
                fields.put(entry.getKey(), entry.getValue());
            }
        }
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            hashData.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).append("&");
        }
        hashData.setLength(hashData.length() - 1);
        String calculatedHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        if (!calculatedHash.equals(vnp_SecureHash)) {
            throw new IllegalStateException("Invalid secure hash");
        }

        // Update payment and order
        Integer orderId = Integer.parseInt(vnp_TxnRef);
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        Payments payment = paymentsRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderId));

        if ("00".equals(vnp_TransactionStatus)) {
            payment.setPaymentStatus("completed");
            payment.setCompletedAt(LocalDateTime.now());
            payment.setTransactionId(params.get("vnp_TransactionNo"));
            order.setPaymentStatus("paid");
            order.setOrderStatus("processing");
        } else {
            payment.setPaymentStatus("failed");
            order.setPaymentStatus("failed");
        }
        paymentsRepository.save(payment);
        orderRepository.save(order);
        logger.info("Processed VNPay callback for order: {}", orderId);
    }

    @Transactional
    public void confirmCODPayment(Integer orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        Payments payment = paymentsRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderId));

        if (!"cod".equalsIgnoreCase(payment.getPaymentMethod())) {
            throw new IllegalStateException("Not a COD payment");
        }

        payment.setPaymentStatus("completed");
        payment.setCompletedAt(LocalDateTime.now());
        order.setPaymentStatus("paid");
        order.setOrderStatus("delivered");
        paymentsRepository.save(payment);
        orderRepository.save(order);
        logger.info("Confirmed COD payment for order: {}", orderId);
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC SHA512", e);
        }
    }
}