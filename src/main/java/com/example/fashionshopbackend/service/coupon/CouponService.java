package com.example.fashionshopbackend.service.coupon;

import com.example.fashionshopbackend.dto.common.ApplyCouponRequest;
import com.example.fashionshopbackend.dto.common.ApplyCouponResponse;
import com.example.fashionshopbackend.dto.common.CouponDTO;
import com.example.fashionshopbackend.entity.common.Coupon;
import com.example.fashionshopbackend.entity.customer.Order;
import com.example.fashionshopbackend.entity.customer.OrderCoupon;
import com.example.fashionshopbackend.entity.customer.CustomerCoupon;
import com.example.fashionshopbackend.repository.coupon.CouponRepository;
import com.example.fashionshopbackend.repository.order.OrderRepository;
import com.example.fashionshopbackend.repository.ordercoupon.OrderCouponRepository;
import com.example.fashionshopbackend.repository.CustomerCouponRepository;
import com.example.fashionshopbackend.util.jwt.JWTUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderCouponRepository orderCouponRepository;

    @Autowired
    private CustomerCouponRepository customerCouponRepository; // Thêm repository mới

    @Autowired
    private JWTUtil jwtUtil;

    // --- API cho admin ---
    @Transactional
    public Coupon createCoupon(CouponDTO dto) {
        if (dto.getExpiryDate() == null) {
            throw new IllegalArgumentException("Expiry date is required");
        }

        Coupon coupon = convertToEntity(dto);
        couponRepository.save(coupon);

        return coupon;
    }

    @Transactional
    public void updateCoupon(CouponDTO dto) {
        Coupon coupon = couponRepository.findById(dto.getCouponId())
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found: " + dto.getCouponId()));

        if (dto.getExpiryDate() == null) {
            throw new IllegalArgumentException("Expiry date is required");
        }

        if (coupon.getExpiryDate().isBefore(LocalDate.now()) && !dto.getExpiryDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot update an expired coupon without extending expiry date");
        }

        coupon.setCode(dto.getCode());
        coupon.setDiscountPercent(dto.getDiscountPercent());
        coupon.setMinOrderValue(dto.getMinOrderValue());
        coupon.setMaxUses(dto.getMaxUses());
        coupon.setUsedCount(dto.getUsedCount());
        coupon.setExpiryDate(dto.getExpiryDate());
        couponRepository.save(coupon);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found: " + id));
        couponRepository.deleteById(id);
    }

    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void exportDataToExcel(HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Coupons");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Coupon ID", "Code", "Discount Percent", "Min Order Value", "Max Uses", "Used Count", "Expiry Date", "Created At"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        List<Coupon> coupons = couponRepository.findAll();
        int rowNum = 1;
        for (Coupon coupon : coupons) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(coupon.getCouponId());
            row.createCell(1).setCellValue(coupon.getCode());
            row.createCell(2).setCellValue(coupon.getDiscountPercent());
            row.createCell(3).setCellValue(coupon.getMinOrderValue());
            row.createCell(4).setCellValue(coupon.getMaxUses());
            row.createCell(5).setCellValue(coupon.getUsedCount());
            row.createCell(6).setCellValue(coupon.getExpiryDate().toString());
            row.createCell(7).setCellValue(coupon.getCreatedAt() != null ? coupon.getCreatedAt().toString() : "");
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=coupons.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // --- API cho người dùng ---
    public List<CouponDTO> getCoupons() {
        return couponRepository.findAll().stream()
                .filter(coupon -> !coupon.getExpiryDate().isBefore(LocalDate.now()))
                .filter(coupon -> coupon.getMaxUses() == 0 || coupon.getUsedCount() < coupon.getMaxUses())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public String claimCoupon(Long couponId) {
        Long userId = getCurrentUserId();

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found: " + couponId));

        // Kiểm tra điều kiện nhận mã giảm giá
        if (coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Coupon has expired");
        }
        if (coupon.getMaxUses() > 0 && coupon.getUsedCount() >= coupon.getMaxUses()) {
            throw new IllegalArgumentException("Coupon usage limit reached");
        }

        // Kiểm tra xem khách hàng đã nhận mã này chưa
        if (customerCouponRepository.findByUserIdAndCouponId(userId, couponId).isPresent()) {
            throw new IllegalArgumentException("You have already claimed this coupon");
        }

        // Lưu mã giảm giá đã nhận
        CustomerCoupon customerCoupon = new CustomerCoupon();
        customerCoupon.setUserId(userId);
        customerCoupon.setCouponId(couponId);
        customerCouponRepository.save(customerCoupon);

        return "Coupon claimed successfully: " + coupon.getCode();
    }

    @Transactional
    public ApplyCouponResponse applyCoupon(ApplyCouponRequest request) {
        Long userId = getCurrentUserId();
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to apply a coupon to this order");
        }

        if (orderCouponRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new IllegalArgumentException("A coupon has already been applied to this order");
        }

        Coupon coupon = couponRepository.findByCode(request.getCouponCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid coupon code"));

        // Kiểm tra xem khách hàng đã nhận mã giảm giá này chưa
        if (!customerCouponRepository.findByUserIdAndCouponId(userId, coupon.getCouponId()).isPresent()) {
            throw new IllegalArgumentException("You need to claim this coupon before applying it");
        }

        if (coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Coupon has expired");
        }
        if (coupon.getMaxUses() > 0 && coupon.getUsedCount() >= coupon.getMaxUses()) {
            throw new IllegalArgumentException("Coupon usage limit reached");
        }
        if (order.getTotalAmount() < coupon.getMinOrderValue()) {
            throw new IllegalArgumentException("Order value does not meet minimum requirement for coupon");
        }

        double discountAmount = (coupon.getDiscountPercent() / 100) * order.getTotalAmount();
        double totalAmountAfterDiscount = order.getTotalAmount() - discountAmount;

        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmountAfterDiscount);
        orderRepository.save(order);

        OrderCoupon orderCoupon = new OrderCoupon();
        orderCoupon.setOrderId(order.getOrderId());
        orderCoupon.setCouponId(coupon.getCouponId());
        orderCouponRepository.save(orderCoupon);

        // Tăng usedCount sẽ được xử lý sau khi thanh toán thành công, không tăng ở đây

        ApplyCouponResponse response = new ApplyCouponResponse();
        response.setMessage("Coupon applied successfully");
        response.setDiscountAmount(discountAmount);
        response.setTotalAmountAfterDiscount(totalAmountAfterDiscount);
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

    private Coupon convertToEntity(CouponDTO dto) {
        Coupon coupon = new Coupon();
        coupon.setCouponId(dto.getCouponId());
        coupon.setCode(dto.getCode());
        coupon.setDiscountPercent(dto.getDiscountPercent());
        coupon.setMinOrderValue(dto.getMinOrderValue());
        coupon.setMaxUses(dto.getMaxUses());
        coupon.setUsedCount(dto.getUsedCount());
        coupon.setExpiryDate(dto.getExpiryDate());
        return coupon;
    }

    private CouponDTO convertToDTO(Coupon coupon) {
        CouponDTO dto = new CouponDTO();
        dto.setCouponId(coupon.getCouponId());
        dto.setCode(coupon.getCode());
        dto.setDiscountPercent(coupon.getDiscountPercent());
        dto.setMinOrderValue(coupon.getMinOrderValue());
        dto.setMaxUses(coupon.getMaxUses());
        dto.setUsedCount(coupon.getUsedCount());
        dto.setExpiryDate(coupon.getExpiryDate());
        dto.setCreatedAt(coupon.getCreatedAt() != null ? coupon.getCreatedAt().toString() : null);
        return dto;
    }
}