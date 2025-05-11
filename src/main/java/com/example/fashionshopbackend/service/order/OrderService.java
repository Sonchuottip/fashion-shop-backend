package com.example.fashionshopbackend.service.order;

import com.example.fashionshopbackend.dto.common.ApplyCouponRequest;
import com.example.fashionshopbackend.dto.common.ApplyCouponResponse;
import com.example.fashionshopbackend.dto.customer.OrderDTO;
import com.example.fashionshopbackend.dto.customer.OrderDetailDTO;
import com.example.fashionshopbackend.dto.customer.OrderUpdateDTO;
import com.example.fashionshopbackend.dto.customer.PaymentDTO;
import com.example.fashionshopbackend.dto.common.ShippingDTO;
import com.example.fashionshopbackend.entity.customer.Order;
import com.example.fashionshopbackend.entity.customer.OrderDetail;
import com.example.fashionshopbackend.entity.customer.Payment;
import com.example.fashionshopbackend.entity.common.Shipping;
import com.example.fashionshopbackend.repository.order.OrderDetailRepository;
import com.example.fashionshopbackend.repository.order.OrderRepository;
import com.example.fashionshopbackend.repository.payment.PaymentRepository;
import com.example.fashionshopbackend.repository.shipping.ShippingRepository;
import com.example.fashionshopbackend.service.coupon.CouponService;
import com.example.fashionshopbackend.util.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ShippingRepository shippingRepository;

    @Autowired
    private CouponService couponService;

    @Autowired
    private JWTUtil jwtUtil;

    @Transactional
    public OrderDTO createOrder(OrderDTO dto) {
        Long userId = getCurrentUserId();
        dto.setUserId(userId);
        Order order = convertToEntity(dto);
        order = orderRepository.save(order);
        final Long orderId = order.getOrderId();

        if (dto.getOrderDetails() != null) {
            List<OrderDetail> orderDetails = dto.getOrderDetails().stream()
                    .map(detailDto -> convertToOrderDetailEntity(detailDto, orderId))
                    .collect(Collectors.toList());
            orderDetailRepository.saveAll(orderDetails);
        }

        // Áp dụng mã giảm giá nếu có couponCode
        if (dto.getCouponCode() != null && !dto.getCouponCode().isEmpty()) {
            ApplyCouponRequest couponRequest = new ApplyCouponRequest();
            couponRequest.setOrderId(orderId);
            couponRequest.setCouponCode(dto.getCouponCode());
            ApplyCouponResponse couponResponse = couponService.applyCoupon(couponRequest);

            // Cập nhật lại thông tin đơn hàng sau khi áp dụng mã giảm giá
            order.setDiscountAmount(couponResponse.getDiscountAmount());
            order.setTotalAmount(couponResponse.getTotalAmountAfterDiscount());
            orderRepository.save(order);
        }

        if (dto.getPayment() != null) {
            Payment payment = convertToPaymentEntity(dto.getPayment(), orderId);
            // Đảm bảo amount được thiết lập
            if (payment.getAmount() == null) {
                payment.setAmount(order.getTotalAmount()); // Lấy totalAmount từ Order nếu amount không được cung cấp
            }
            paymentRepository.save(payment);
        }

        if (dto.getShipping() != null) {
            Shipping shipping = convertToShippingEntity(dto.getShipping(), orderId);
            shippingRepository.save(shipping);
        }

        return getOrderById(orderId);
    }

    public OrderDTO getOrderById(Long orderId) {
        Long userId = getCurrentUserId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to view this order");
        }
        OrderDTO dto = convertToDTO(order);
        dto.setOrderDetails(orderDetailRepository.findByOrderId(orderId).stream()
                .map(this::convertToOrderDetailDTO)
                .collect(Collectors.toList()));
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> dto.setPayment(convertToPaymentDTO(payment)));
        shippingRepository.findByOrderId(orderId).ifPresent(shipping -> dto.setShipping(convertToShippingDTO(shipping)));
        return dto;
    }

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByUser() {
        Long userId = getCurrentUserId();
        return orderRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Long userId = getCurrentUserId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to cancel this order");
        }
        order.setOrderStatus("Cancelled");
        orderRepository.save(order);
    }

    @Transactional
    public OrderDTO updateOrder(Long orderId, OrderUpdateDTO dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        order.setOrderStatus(dto.getOrderStatus());
        order = orderRepository.save(order);

        OrderDTO responseDto = convertToDTO(order);
        responseDto.setOrderDetails(orderDetailRepository.findByOrderId(orderId).stream()
                .map(this::convertToOrderDetailDTO)
                .collect(Collectors.toList()));
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> responseDto.setPayment(convertToPaymentDTO(payment)));
        shippingRepository.findByOrderId(orderId).ifPresent(shipping -> responseDto.setShipping(convertToShippingDTO(shipping)));

        return responseDto;
    }

    private Long getCurrentUserId() {
        String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid token: " + e.getMessage());
        }
    }

    private OrderDTO convertToDTO(Order order) {
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
        return dto;
    }

    private Order convertToEntity(OrderDTO dto) {
        Order order = new Order();
        order.setOrderId(dto.getOrderId());
        order.setUserId(dto.getUserId());
        order.setSubTotal(dto.getSubTotal());
        order.setShippingCost(dto.getShippingCost());
        order.setDiscountAmount(dto.getDiscountAmount());
        order.setTotalAmount(dto.getTotalAmount());
        order.setOrderStatus(dto.getOrderStatus());
        order.setPaymentStatus(dto.getPaymentStatus());
        order.setReceiverName(dto.getReceiverName());
        order.setShippingAddress(dto.getShippingAddress());
        order.setPhoneNumber(dto.getPhoneNumber());
        return order;
    }

    private OrderDetail convertToOrderDetailEntity(OrderDetailDTO dto, Long orderId) {
        OrderDetail detail = new OrderDetail();
        detail.setOrderDetailId(dto.getOrderDetailId());
        detail.setOrderId(orderId);
        detail.setVariantId(dto.getVariantId());
        detail.setQuantity(dto.getQuantity());
        detail.setPrice(dto.getPrice());
        return detail;
    }

    private OrderDetailDTO convertToOrderDetailDTO(OrderDetail detail) {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setOrderDetailId(detail.getOrderDetailId());
        dto.setOrderId(detail.getOrderId());
        dto.setVariantId(detail.getVariantId());
        dto.setQuantity(detail.getQuantity());
        dto.setPrice(detail.getPrice());
        return dto;
    }

    private Payment convertToPaymentEntity(PaymentDTO dto, Long orderId) {
        Payment payment = new Payment();
        payment.setPaymentId(dto.getPaymentId());
        payment.setOrderId(orderId);
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setTransactionId(dto.getTransactionId());
        payment.setPaymentStatus(dto.getPaymentStatus());
        payment.setAmount(dto.getAmount());
        return payment;
    }

    private PaymentDTO convertToPaymentDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setOrderId(payment.getOrderId());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setAmount(payment.getAmount());
        return dto;
    }

    private Shipping convertToShippingEntity(ShippingDTO dto, Long orderId) {
        Shipping shipping = new Shipping();
        shipping.setShippingId(dto.getShippingId());
        shipping.setOrderId(orderId);
        shipping.setTrackingNumber(dto.getTrackingNumber());
        shipping.setCarrier(dto.getCarrier());
        shipping.setShippingCost(dto.getShippingCost());
        shipping.setShippingAddress(dto.getShippingAddress());
        shipping.setEstimatedDelivery(dto.getEstimatedDelivery());
        shipping.setShippingStatus(dto.getShippingStatus());
        return shipping;
    }

    private ShippingDTO convertToShippingDTO(Shipping shipping) {
        ShippingDTO dto = new ShippingDTO();
        dto.setShippingId(shipping.getShippingId());
        dto.setOrderId(shipping.getOrderId());
        dto.setTrackingNumber(shipping.getTrackingNumber());
        dto.setCarrier(shipping.getCarrier());
        dto.setShippingCost(shipping.getShippingCost());
        dto.setShippingAddress(shipping.getShippingAddress());
        dto.setEstimatedDelivery(shipping.getEstimatedDelivery());
        dto.setShippingStatus(shipping.getShippingStatus());
        return dto;
    }
}