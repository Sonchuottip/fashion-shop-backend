package com.example.fashionshopbackend.service.admin;

import com.example.fashionshopbackend.dto.coupon.CouponDTO;
import com.example.fashionshopbackend.entity.coupon.Coupon;
import com.example.fashionshopbackend.repository.coupon.CouponRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    public void createCoupon(CouponDTO dto) {
        Coupon coupon = convertToEntity(dto);
        couponRepository.save(coupon);
    }

    public void updateCoupon(CouponDTO dto) {
        Coupon coupon = couponRepository.findById(dto.getCouponId())
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found: " + dto.getCouponId()));
        coupon.setCode(dto.getCode());
        coupon.setDiscountPercent(dto.getDiscountPercent());
        coupon.setMinOrderValue(dto.getMinOrderValue());
        coupon.setMaxUses(dto.getMaxUses());
        coupon.setUsedCount(dto.getUsedCount());
        coupon.setExpiryDate(dto.getExpiryDate());
        couponRepository.save(coupon);
    }

    public void deleteCoupon(Integer id) {
        couponRepository.deleteById(id);
    }

    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
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
            row.createCell(7).setCellValue(coupon.getCreatedAt().toString());
        }

        // Ghi file và gửi response
        workbook.write(response.getOutputStream());
        workbook.close();
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
        return dto;
    }
}