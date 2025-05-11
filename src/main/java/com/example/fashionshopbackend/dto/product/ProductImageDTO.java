package com.example.fashionshopbackend.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductImageDTO {
    private Integer imageId; // Dùng cho cập nhật, có thể null khi tạo mới
    private Integer productId; // Liên kết với sản phẩm
    private String imageUrl;
    private Boolean isPrimary;
    private MultipartFile file;
}