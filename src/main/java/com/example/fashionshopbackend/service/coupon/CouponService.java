package com.example.fashionshopbackend.service.coupon;

import com.example.fashionshopbackend.dto.coupon.ApplyCouponRequest;
import com.example.fashionshopbackend.dto.coupon.ApplyCouponResponse;
import com.example.fashionshopbackend.dto.coupon.CouponDTO;
import com.example.fashionshopbackend.entity.adminlog.AdminLog;
import com.example.fashionshopbackend.entity.coupon.Coupon;
import com.example.fashionshopbackend.entity.order.Order;
import com.example.fashionshopbackend.entity.ordercoupon.OrderCoupon;
import com.example.fashionshopbackend.repository.adminlog.AdminLogRepository;
import com.example.fashionshopbackend.repository.coupon.CouponRepository;
import com.example.fashionshopbackend.repository.order.OrderRepository;
import com.example.fashionshopbackend.repository.ordercoupon.OrderCouponRepository;
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
    private AdminLogRepository adminLogRepository;

    @Autowired
    private JWTUtil jwtUtil;

    // --- API cho admin ---
    @Transactional
    public Coupon createCoupon(CouponDTO dto) {
        // Kiểm tra expiryDate
        if (dto.getExpiryDate() == null) {
            throw new IllegalArgumentException("Expiry date is required");
        }

        Coupon coupon = convertToEntity(dto);
        couponRepository.save(coupon);

        // Ghi log hành động admin
        logAdminAction("Created coupon: " + coupon.getCode());

        return coupon; // Trả về đối tượng đã lưu
    }

    @Transactional
    public void updateCoupon(CouponDTO dto) {
        Coupon coupon = couponRepository.findById(dto.getCouponId())
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found: " + dto.getCouponId()));

        // Kiểm tra expiryDate
        if (dto.getExpiryDate() == null) {
            throw new IllegalArgumentException("Expiry date is required");
        }

        // Kiểm tra trạng thái mã giảm giá
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

        // Ghi log hành động admin
        logAdminAction("Updated coupon: " + coupon.getCode());
    }

    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found: " + id));
        couponRepository.deleteById(id);

        // Ghi log hành động admin
        logAdminAction("Deleted coupon: " + coupon.getCode());
    }

    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void exportDataToExcel(HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Coupons");

        // Tạo header
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Coupon ID", "Code", "Discount Percent", "Min Order Value", "Max Uses", "Used Count", "Expiry Date", "Created At"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Đổ dữ liệu
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

        // Ghi file và gửi response
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
    public ApplyCouponResponse applyCoupon(ApplyCouponRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (orderCouponRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new IllegalArgumentException("A coupon has already been applied to this order");
        }

        Coupon coupon = couponRepository.findByCode(request.getCouponCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid coupon code"));

        // Kiểm tra điều kiện áp dụng mã giảm giá
        if (coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Coupon has expired");
        }
        if (coupon.getMaxUses() > 0 && coupon.getUsedCount() >= coupon.getMaxUses()) {
            throw new IllegalArgumentException("Coupon usage limit reached");
        }
        if (order.getTotalAmount() < coupon.getMinOrderValue()) {
            throw new IllegalArgumentException("Order value does not meet minimum requirement for coupon");
        }

        // Tính toán giảm giá
        double discountAmount = (coupon.getDiscountPercent() / 100) * order.getTotalAmount();
        double totalAmountAfterDiscount = order.getTotalAmount() - discountAmount;

        // Cập nhật đơn hàng
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmountAfterDiscount);
        orderRepository.save(order);

        // Lưu vào bảng OrderCoupons
        OrderCoupon orderCoupon = new OrderCoupon();
        orderCoupon.setOrderId(order.getOrderId());
        orderCoupon.setCouponId(coupon.getCouponId());
        orderCouponRepository.save(orderCoupon);

        // Cập nhật số lần sử dụng mã giảm giá
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        ApplyCouponResponse response = new ApplyCouponResponse();
        response.setMessage("Coupon applied successfully");
        response.setDiscountAmount(discountAmount);
        response.setTotalAmountAfterDiscount(totalAmountAfterDiscount);
        return response;
    }

    // --- Các phương thức hỗ trợ ---
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

    private Long getCurrentUserId() {
        String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid token: " + e.getMessage());
        }
    }

    private void logAdminAction(String action) {
        Long adminId = getCurrentUserId();
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction(action);
        adminLogRepository.save(log);
    }
}