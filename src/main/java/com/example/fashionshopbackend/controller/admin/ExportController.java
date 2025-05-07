package com.example.fashionshopbackend.controller.admin;

import com.example.fashionshopbackend.dto.category.CategoryDTO;
import com.example.fashionshopbackend.dto.coupon.CouponDTO;
import com.example.fashionshopbackend.dto.product.ProductWithImagesAndVariantsDTO;
import com.example.fashionshopbackend.dto.user.UserDTO;
import com.example.fashionshopbackend.service.admin.CategoryService;
import com.example.fashionshopbackend.service.admin.ProductService;
import com.example.fashionshopbackend.service.admin.UserService;
import com.example.fashionshopbackend.service.coupon.CouponService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/export")
@PreAuthorize("hasAuthority('Admin')")
public class ExportController {

    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public void exportData(HttpServletResponse response, @RequestParam String type) throws IOException {
        try {
            logger.debug("Exporting data to Excel, type: {}", type);
            Workbook workbook = new XSSFWorkbook();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + type + "_export_" + System.currentTimeMillis() + ".xlsx");

            switch (type.toLowerCase()) {
                case "coupons":
                    exportCoupons(workbook);
                    break;
                case "users":
                    exportUsers(workbook);
                    break;
                case "categories":
                    exportCategories(workbook);
                    break;
                case "products":
                    exportProducts(workbook);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid export type: " + type);
            }

            workbook.write(response.getOutputStream());
            workbook.close();
            logger.info("Data exported successfully for type: {}", type);
        } catch (Exception e) {
            logger.error("Error exporting data: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error exporting data: " + e.getMessage());
        }
    }

    private void exportCoupons(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Coupons");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Coupon ID", "Code", "Discount Percent", "Min Order Value", "Max Uses", "Used Count", "Expiry Date", "Created At"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        List<CouponDTO> coupons = couponService.getAllCoupons();
        int rowNum = 1;
        for (CouponDTO coupon : coupons) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(coupon.getCouponId());
            row.createCell(1).setCellValue(coupon.getCode());
            row.createCell(2).setCellValue(coupon.getDiscountPercent());
            row.createCell(3).setCellValue(coupon.getMinOrderValue());
            row.createCell(4).setCellValue(coupon.getMaxUses());
            row.createCell(5).setCellValue(coupon.getUsedCount());
            row.createCell(6).setCellValue(coupon.getExpiryDate().toString());
            row.createCell(7).setCellValue(coupon.getCreatedAt() != null ? coupon.getCreatedAt() : "");
        }
    }

    private void exportUsers(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Users");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"User ID", "Email", "Full Name", "Role"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        List<UserDTO> users = userService.getAllUsers();
        int rowNum = 1;
        for (UserDTO user : users) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getUserId());
            row.createCell(1).setCellValue(user.getEmail());
            row.createCell(2).setCellValue(user.getFullName());
            row.createCell(3).setCellValue(user.getRole());
        }
    }

    private void exportCategories(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Categories");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Category ID", "Name", "Description"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        List<CategoryDTO> categories = categoryService.getAllCategories();
        int rowNum = 1;
        for (CategoryDTO category : categories) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(category.getCategoryId());
            row.createCell(1).setCellValue(category.getName());
            row.createCell(2).setCellValue(category.getDescription());
        }
    }

    private void exportProducts(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Products");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Product ID", "Name", "Description", "Price", "Category ID"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        List<ProductWithImagesAndVariantsDTO> products = productService.getAllProductsWithImagesAndVariants();
        int rowNum = 1;
        for (ProductWithImagesAndVariantsDTO product : products) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(product.getProductId());
            row.createCell(1).setCellValue(product.getName());
            row.createCell(2).setCellValue(product.getDescription());
            row.createCell(3).setCellValue(product.getPrice());
            row.createCell(4).setCellValue(product.getCategoryId());
        }
    }
}