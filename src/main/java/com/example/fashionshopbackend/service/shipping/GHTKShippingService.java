package com.example.fashionshopbackend.service.shipping;

import com.example.fashionshopbackend.entity.order.OrderDetails;
import com.example.fashionshopbackend.entity.order.Orders;
import com.example.fashionshopbackend.entity.shipping.Shipping;
import com.example.fashionshopbackend.repository.OrderDetailsRepository;
import com.example.fashionshopbackend.repository.OrderRepository;
import com.example.fashionshopbackend.repository.PaymentsRepository;
import com.example.fashionshopbackend.repository.ShippingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.fashionshopbackend.entity.payment.Payments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GHTKShippingService {

    private static final Logger logger = LoggerFactory.getLogger(GHTKShippingService.class);

    @Value("${ghtk.api-token}")
    private String ghtkApiToken;

    @Value("${ghtk.api-url}")
    private String ghtkApiUrl;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private PaymentsRepository paymentsRepository;

    @Autowired
    private ShippingRepository shippingRepository;

    // Default dimensions for clothing items (in cm)
    private static final double DEFAULT_LENGTH = 30.0;
    private static final double DEFAULT_WIDTH = 20.0;
    private static final double DEFAULT_HEIGHT = 5.0;
    private static final double VOLUMETRIC_DIVISOR = 6000.0;

    public BigDecimal calculateShippingFee(Integer orderId, String pickProvince, String pickDistrict, String province, String district, String address) throws Exception {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        List<OrderDetails> details = orderDetailsRepository.findByOrderId(orderId);

        // Calculate volumetric weight
        double totalVolumetricWeight = details.stream()
                .mapToDouble(detail -> (DEFAULT_LENGTH * DEFAULT_WIDTH * DEFAULT_HEIGHT * detail.getQuantity()) / VOLUMETRIC_DIVISOR)
                .sum();
        int weightInGrams = (int) (totalVolumetricWeight * 1000);

        // Prepare request
        Map<String, Object> params = new HashMap<>();
        params.put("pick_province", pickProvince);
        params.put("pick_district", pickDistrict);
        params.put("province", province);
        params.put("district", district);
        params.put("address", address);
        params.put("weight", weightInGrams);
        params.put("value", order.getSubTotal().intValue());
        params.put("transport", "road");
        params.put("deliver_option", "none");

        String query = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue().toString(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        String url = ghtkApiUrl + "/services/shipment/fee?" + query;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Token", ghtkApiToken)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseBody = mapper.readValue(response.body(), Map.class);
            if ((Boolean) responseBody.get("success")) {
                Map<String, Object> fee = (Map<String, Object>) responseBody.get("fee");
                return new BigDecimal(fee.get("fee").toString());
            } else {
                throw new IllegalStateException("Failed to calculate shipping fee: " + responseBody.get("message"));
            }
        } else {
            throw new IllegalStateException("GHTK API error: " + response.body());
        }
    }

    @Transactional
    public String createShippingOrder(Integer orderId, String pickProvince, String pickDistrict, String pickAddress, String pickTel) throws Exception {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        List<OrderDetails> details = orderDetailsRepository.findByOrderId(orderId);
        Payments payment = paymentsRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderId));

        // Calculate volumetric weight
        double totalVolumetricWeight = details.stream()
                .mapToDouble(detail -> (DEFAULT_LENGTH * DEFAULT_WIDTH * DEFAULT_HEIGHT * detail.getQuantity()) / VOLUMETRIC_DIVISOR)
                .sum();
        int weightInGrams = (int) (totalVolumetricWeight * 1000);

        // Prepare payload
        Map<String, Object> payload = new HashMap<>();
        List<Map<String, Object>> products = details.stream().map(detail -> {
            Map<String, Object> product = new HashMap<>();
            product.put("name", "Clothing Item");
            product.put("weight", totalVolumetricWeight / details.size());
            product.put("quantity", detail.getQuantity());
            product.put("product_code", "CLOTH_" + detail.getVariantId());
            return product;
        }).collect(Collectors.toList());
        payload.put("products", products);

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("id", "ORDER_" + orderId);
        orderData.put("pick_name", "Shop Name");
        orderData.put("pick_address", pickAddress);
        orderData.put("pick_province", pickProvince);
        orderData.put("pick_district", pickDistrict);
        orderData.put("pick_tel", pickTel);
        orderData.put("tel", order.getPhoneNumber());
        orderData.put("name", order.getReceiverName());
        orderData.put("address", order.getShippingAddress());
        orderData.put("province", order.getShippingAddress().split(",")[1].trim());
        orderData.put("district", order.getShippingAddress().split(",")[0].trim());
        orderData.put("is_freeship", "0");
        orderData.put("value", order.getSubTotal().intValue());
        orderData.put("transport", "road");
        // Set pick_money for COD
        if ("cod".equalsIgnoreCase(payment.getPaymentMethod())) {
            orderData.put("pick_money", order.getTotalAmount().intValue());
        } else {
            orderData.put("pick_money", 0);
        }
        payload.put("order", orderData);

        // Send request to GHTK
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ghtkApiUrl + "/services/shipment/order"))
                .header("Content-Type", "application/json")
                .header("Token", ghtkApiToken)
                .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseBody = mapper.readValue(response.body(), Map.class);
            if ((Boolean) responseBody.get("success")) {
                Map<String, Object> orderResponse = (Map<String, Object>) responseBody.get("order");
                String labelId = orderResponse.get("label").toString();
                BigDecimal shippingFee = new BigDecimal(orderResponse.get("fee").toString());

                // Update shipping and order
                Shipping shipping = new Shipping();
                shipping.setOrderId(orderId);
                shipping.setTrackingNumber(labelId);
                shipping.setCarrier("GHTK");
                shipping.setShippingCost(shippingFee);
                shipping.setShippingAddress(order.getShippingAddress());
                shipping.setShippingStatus("pending");
                shippingRepository.save(shipping);

                order.setShippingCost(shippingFee);
                order.setTotalAmount(order.getSubTotal().add(shippingFee).subtract(order.getDiscountAmount()));
                orderRepository.save(order);

                logger.info("Created GHTK shipping order: {}", labelId);
                return labelId;
            } else {
                throw new IllegalStateException("Failed to create shipping order: " + responseBody.get("message"));
            }
        } else {
            throw new IllegalStateException("GHTK API error: " + response.body());
        }
    }

    @Transactional
    public void handleShippingWebhook(Map<String, Object> payload) {
        String labelId = (String) payload.get("label_id");
        Integer statusId = (Integer) payload.get("status_id");
        String status;
        switch (statusId) {
            case 2: status = "pending"; break;
            case 4: status = "shipped"; break;
            case 5: status = "delivered"; break;
            default: status = "pending";
        }

        Shipping shipping = shippingRepository.findByTrackingNumber(labelId)
                .orElseThrow(() -> new IllegalArgumentException("Shipping not found: " + labelId));
        shipping.setShippingStatus(status);
        shippingRepository.save(shipping);

        Orders order = orderRepository.findById(shipping.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + shipping.getOrderId()));
        order.setOrderStatus(status);
        if ("delivered".equals(status) && "cod".equalsIgnoreCase(paymentsRepository.findByOrderId(order.getOrderId()).get().getPaymentMethod())) {
            confirmCODPayment(order.getOrderId());
        }
        orderRepository.save(order);

        logger.info("Updated shipping status for tracking number: {}", labelId);
    }

    private void confirmCODPayment(Integer orderId) {
        // Logic xác nhận thanh toán COD
        Payments payment = paymentsRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderId));
        payment.setPaymentStatus("paid");
        paymentsRepository.save(payment);
        logger.info("Confirmed COD payment for order: {}", orderId);
    }
}