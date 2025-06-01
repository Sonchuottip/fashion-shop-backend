package com.example.fashionshopbackend.service.product;

import com.example.fashionshopbackend.dto.product.ProductImageDTO;
import com.example.fashionshopbackend.entity.product.Product;
import com.example.fashionshopbackend.entity.product.ProductImage;
import com.example.fashionshopbackend.repository.ProductRepository;
import com.example.fashionshopbackend.util.CloudinaryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductImageService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CloudinaryUtils cloudinaryUtils;

    @Transactional
    @Async
    public List<ProductImageDTO> addProductImages(Integer productId, List<MultipartFile> newImages) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        List<ProductImage> productImages = product.getImages();
        Map<ProductImage, String> imagePublicIds = new HashMap<>();
        List<ProductImageDTO> addedImageDTOs = new ArrayList<>();

        try {
            for (MultipartFile image : newImages) {
                String publicId = "product/" + productId + "/" + UUID.randomUUID().toString();
                String imageUrl = cloudinaryUtils.uploadImage(image.getBytes(), publicId);
                ProductImage productImage = new ProductImage();
                productImage.setProduct(product);
                productImage.setImageUrl(imageUrl);
                productImage.setIsPrimary(productImages.isEmpty());
                productImage.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                productImages.add(productImage);
                imagePublicIds.put(productImage, publicId);
                addedImageDTOs.add(convertToImageDTO(productImage)); // Thêm DTO ngay sau khi tạo
            }
            product.setImages(productImages);
            productRepository.save(product);
            return addedImageDTOs;
        } catch (Exception e) {
            for (Map.Entry<ProductImage, String> entry : imagePublicIds.entrySet()) {
                try {
                    cloudinaryUtils.deleteImage(entry.getValue());
                } catch (IOException ex) {
                    System.err.println("Failed to delete image from Cloudinary: " + entry.getValue() + ", error: " + ex.getMessage());
                }
            }
            throw new RuntimeException("Lỗi khi thêm ảnh: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteImage(Integer productId, Integer imageId) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        ProductImage imageToDelete = product.getImages().stream()
                .filter(img -> img.getImageId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Ảnh không tồn tại"));
        cloudinaryUtils.deleteImage(extractPublicId(imageToDelete.getImageUrl()));
        product.getImages().remove(imageToDelete);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<ProductImageDTO> getAllImages(Integer productId) {
        return productRepository.findById(productId)
                .map(product -> product.getImages().stream()
                        .map(this::convertToImageDTO)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
    }

    public ProductImageDTO convertToImageDTO(ProductImage image) {
        ProductImageDTO dto = new ProductImageDTO();
        dto.setImageId(image.getImageId());
        dto.setImageUrl(image.getImageUrl());
        dto.setIsPrimary(image.getIsPrimary());
        dto.setCreatedAt(image.getCreatedAt());
        return dto;
    }

    private String extractPublicId(String imageUrl) {
        String[] parts = imageUrl.split("/");
        String publicId = parts[parts.length - 1].split("\\.")[0];
        return publicId;
    }
}