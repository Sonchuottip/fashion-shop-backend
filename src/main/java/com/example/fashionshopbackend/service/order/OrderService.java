package com.example.fashionshopbackend.service.order;

import com.example.fashionshopbackend.dto.order.OrderDTO;
import com.example.fashionshopbackend.dto.order.OrderDetailDTO;
import com.example.fashionshopbackend.entity.order.OrderDetails;
import com.example.fashionshopbackend.entity.order.Orders;
import com.example.fashionshopbackend.entity.coupon.Coupon;
import com.example.fashionshopbackend.entity.coupon.OrderCoupons;
import com.example.fashionshopbackend.entity.payment.Payments;
import com.example.fashionshopbackend.repository.PaymentsRepository;
import com.example.fashionshopbackend.entity.coupon.CouponUsage;
import com.example.fashionshopbackend.repository.OrderRepository;
import com.example.fashionshopbackend.repository.OrderDetailsRepository;
import com.example.fashionshopbackend.repository.CouponRepository;
import com.example.fashionshopbackend.repository.OrderCouponsRepository;
import com.example.fashionshopbackend.repository.CouponUsageRepository;
import com.example.fashionshopbackend.service.product.PromotionService;
import com.example.fashionshopbackend.service.payment.PaymentService;
import com.example.fashionshopbackend.service.shipping.GHTKShippingService;
import com.example.fashionshopbackend.entity.product.ProductVariant;
import com.example.fashionshopbackend.repository.ProductVariantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private OrderCouponsRepository orderCouponsRepository;

    @Autowired
    private CouponUsageRepository couponUsageRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentsRepository paymentsRepository;

    @Autowired
    private GHTKShippingService ghtkShippingService;

    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO, String couponCode, String paymentMethod, String ipAddress, String pickProvince, String pickDistrict, String pickAddress, String pickTel) throws Exception {
        if (orderDTO.getUserId() == null || orderDTO.getOrderDetails() == null || orderDTO.getOrderDetails().isEmpty()) {
            throw new IllegalArgumentException("Invalid order details");
        }
        if (!List.of("vnpay", "cod").contains(paymentMethod.toLowerCase())) {
            throw new IllegalArgumentException("Invalid payment method: " + paymentMethod);
        }

        Orders order = new Orders();
        order.setUserId(orderDTO.getUserId());
        order.setReceiverName(orderDTO.getReceiverName());
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setPhoneNumber(orderDTO.getPhoneNumber());
        order.setOrderStatus("pending");
        order.setPaymentStatus("pending");

        BigDecimal subTotal = BigDecimal.ZERO;
        for (OrderDetailDTO detailDTO : orderDTO.getOrderDetails()) {
            ProductVariant variant = productVariantRepository.findById(Long.valueOf(detailDTO.getVariantId()))
                    .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + detailDTO.getVariantId()));
            if (!"active".equals(variant.getStatus())) {
                throw new IllegalArgumentException("Variant is not active: " + detailDTO.getVariantId());
            }
            if (variant.getStock() < detailDTO.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for variant: " + detailDTO.getVariantId());
            }
            BigDecimal price = promotionService.calculateDiscountedPrice(
                    variant.getPrice(),
                    promotionService.getApplicablePromotion(variant.getProduct()).orElse(null)
            );
            subTotal = subTotal.add(price.multiply(BigDecimal.valueOf(detailDTO.getQuantity())));
            // Update stock
            variant.setStock(variant.getStock() - detailDTO.getQuantity());
            productVariantRepository.save(variant);
        }
        order.setSubTotal(subTotal);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (couponCode != null && !couponCode.isEmpty()) {
            Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(couponCode)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or inactive coupon code"));
            if (coupon.getExpiryDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Coupon expired");
            }
            if (coupon.getUsedCount() >= coupon.getMaxUses()) {
                throw new IllegalArgumentException("Coupon usage limit reached");
            }
            long userUsageCount = couponUsageRepository.countByCouponIdAndUserId(coupon.getCouponId(), orderDTO.getUserId());
            if (userUsageCount >= coupon.getMaxUsesPerUser()) {
                throw new IllegalArgumentException("User has reached coupon usage limit");
            }
            if (subTotal.compareTo(coupon.getMinOrderValue()) < 0) {
                throw new IllegalArgumentException("Order value does not meet minimum requirement");
            }

            if ("percent".equals(coupon.getDiscountType())) {
                discountAmount = subTotal.multiply(coupon.getDiscountPercent().divide(BigDecimal.valueOf(100)));
            } else {
                discountAmount = coupon.getDiscountAmount();
            }

            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);

            order.setDiscountAmount(discountAmount);
        }

        BigDecimal shippingCost = ghtkShippingService.calculateShippingFee(
                order.getOrderId(),
                pickProvince,
                pickDistrict,
                order.getShippingAddress().split(",")[1].trim(),
                order.getShippingAddress().split(",")[0].trim(),
                order.getShippingAddress()
        );
        order.setShippingCost(shippingCost);

        order.setTotalAmount(subTotal.add(shippingCost).subtract(discountAmount));
        Orders savedOrder = orderRepository.save(order);

        for (OrderDetailDTO detailDTO : orderDTO.getOrderDetails()) {
            ProductVariant variant = productVariantRepository.findById(Long.valueOf(detailDTO.getVariantId())).get();
            BigDecimal price = promotionService.calculateDiscountedPrice(
                    variant.getPrice(),
                    promotionService.getApplicablePromotion(variant.getProduct()).orElse(null)
            );
            OrderDetails detail = new OrderDetails();
            detail.setOrderId(savedOrder.getOrderId());
            detail.setVariantId(detailDTO.getVariantId());
            detail.setQuantity(detailDTO.getQuantity());
            detail.setPrice(price);
            orderDetailsRepository.save(detail);
        }

        if (couponCode != null && !couponCode.isEmpty()) {
            Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(couponCode).get();
            OrderCoupons orderCoupon = new OrderCoupons();
            orderCoupon.setOrderId(savedOrder.getOrderId());
            orderCoupon.setCouponId(coupon.getCouponId());
            orderCouponsRepository.save(orderCoupon);

            CouponUsage usage = new CouponUsage();
            usage.setCouponId(coupon.getCouponId());
            usage.setUserId(orderDTO.getUserId());
            usage.setOrderId(savedOrder.getOrderId());
            couponUsageRepository.save(usage);
        }

        String paymentUrl = paymentService.initiatePayment(savedOrder.getOrderId(), paymentMethod, ipAddress);
        String trackingNumber = ghtkShippingService.createShippingOrder(
                savedOrder.getOrderId(),
                pickProvince,
                pickDistrict,
                pickAddress,
                pickTel
        );

        logger.info("Created order: {}, payment URL: {}, tracking number: {}", savedOrder.getOrderId(), paymentUrl, trackingNumber);
        OrderDTO result = convertToOrderDTO(savedOrder);
        result.setPaymentUrl(paymentUrl);
        return result;
    }

    public OrderDTO getOrderById(Integer orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        return convertToOrderDTO(order);
    }

    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::convertToOrderDTO);
    }

    public Page<OrderDTO> getOrdersByStatus(String status, Pageable pageable) {
        if (!List.of("pending", "processing", "shipped", "delivered", "cancelled").contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        return orderRepository.findByOrderStatus(status, pageable)
                .map(this::convertToOrderDTO);
    }

    public Page<OrderDTO> getOrdersByDate(LocalDate date, Pageable pageable) {
        return orderRepository.findByCreatedAtDate(date, pageable)
                .map(this::convertToOrderDTO);
    }

    public Page<OrderDTO> getOrdersByMonthAndYear(int year, int month, Pageable pageable) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }
        return orderRepository.findByCreatedAtMonthAndYear(year, month, pageable)
                .map(this::convertToOrderDTO);
    }

    public Page<OrderDTO> getOrdersByYear(int year, Pageable pageable) {
        return orderRepository.findByCreatedAtYear(year, pageable)
                .map(this::convertToOrderDTO);
    }

    @Transactional
    public OrderDTO cancelOrder(Integer orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        Payments payment = paymentsRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderId));
        if (!order.getOrderStatus().equals("pending") || payment.getPaymentStatus().equals("completed")) {
            throw new IllegalArgumentException("Only pending orders with pending payment can be cancelled");
        }
        order.setOrderStatus("cancelled");
        Orders savedOrder = orderRepository.save(order);
        // Restore stock
        List<OrderDetails> details = orderDetailsRepository.findByOrderId(orderId);
        for (OrderDetails detail : details) {
            ProductVariant variant = productVariantRepository.findById(Long.valueOf(detail.getVariantId())).get();
            variant.setStock(variant.getStock() + detail.getQuantity());
            productVariantRepository.save(variant);
        }
        logger.info("Cancelled order: {}", orderId);
        return convertToOrderDTO(savedOrder);
    }

    @Transactional
    public OrderDTO updateOrder(Integer orderId, OrderDTO orderDTO) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (orderDTO.getReceiverName() != null) {
            order.setReceiverName(orderDTO.getReceiverName());
        }
        if (orderDTO.getShippingAddress() != null) {
            order.setShippingAddress(orderDTO.getShippingAddress());
        }
        if (orderDTO.getPhoneNumber() != null) {
            order.setPhoneNumber(orderDTO.getPhoneNumber());
        }
        if (orderDTO.getOrderStatus() != null &&
                List.of("pending", "processing", "shipped", "delivered", "cancelled").contains(orderDTO.getOrderStatus())) {
            order.setOrderStatus(orderDTO.getOrderStatus());
        }
        if (orderDTO.getShippingCost() != null) {
            order.setShippingCost(orderDTO.getShippingCost());
            order.setTotalAmount(order.getSubTotal().add(orderDTO.getShippingCost()).subtract(order.getDiscountAmount()));
        }

        Orders savedOrder = orderRepository.save(order);
        logger.info("Updated order: {}", orderId);
        return convertToOrderDTO(savedOrder);
    }

    private OrderDTO convertToOrderDTO(Orders order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setSubTotal(order.getSubTotal());
        dto.setShippingCost(order.getShippingCost());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setReceiverName(order.getReceiverName());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setPhoneNumber(order.getPhoneNumber());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        List<OrderDetails> details = orderDetailsRepository.findByOrderId(order.getOrderId());
        List<OrderDetailDTO> detailDTOs = details.stream().map(detail -> {
            OrderDetailDTO detailDTO = new OrderDetailDTO();
            detailDTO.setOrderDetailId(detail.getOrderDetailId());
            detailDTO.setVariantId(detail.getVariantId());
            detailDTO.setQuantity(detail.getQuantity());
            detailDTO.setPrice(detail.getPrice());
            return detailDTO;
        }).collect(Collectors.toList());
        dto.setOrderDetails(detailDTOs);

        orderCouponsRepository.findByOrderId(order.getOrderId())
                .ifPresent(orderCoupon -> {
                    couponRepository.findById(orderCoupon.getCouponId())
                            .ifPresent(coupon -> dto.setCouponCode(coupon.getCode()));
                });

        return dto;
    }
}