package com.example.fashionshopbackend.service.admin;

import com.example.fashionshopbackend.dto.product.ProductImageDTO;
import com.example.fashionshopbackend.dto.product.ProductVariantDTO;
import com.example.fashionshopbackend.dto.product.ProductWithImagesAndVariantsDTO;
import com.example.fashionshopbackend.entity.product.Product;
import com.example.fashionshopbackend.entity.product.ProductImage;
import com.example.fashionshopbackend.entity.product.ProductVariant;
import com.example.fashionshopbackend.repository.product.ProductImageRepository;
import com.example.fashionshopbackend.repository.product.ProductRepository;
import com.example.fashionshopbackend.repository.product.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    public List<ProductWithImagesAndVariantsDTO> getAllProductsWithImagesAndVariants() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public void createProductWithImagesAndVariants(ProductWithImagesAndVariantsDTO dto) {
        Product product = convertToEntity(dto);
        product = productRepository.save(product);
        final Integer productId = product.getProductId(); // Biến final để sử dụng trong lambda

        // Lưu ảnh
        if (dto.getImages() != null) {
            List<ProductImage> images = dto.getImages().stream()
                    .map(imageDto -> convertToImageEntity(imageDto, productId))
                    .collect(Collectors.toList());
            productImageRepository.saveAll(images);
        }

        // Lưu biến thể
        if (dto.getVariants() != null) {
            List<ProductVariant> variants = dto.getVariants().stream()
                    .map(variantDto -> convertToVariantEntity(variantDto, productId))
                    .collect(Collectors.toList());
            productVariantRepository.saveAll(variants);
        }
    }

    public void updateProductWithImagesAndVariants(ProductWithImagesAndVariantsDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + dto.getProductId()));
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategoryId(dto.getCategoryId());
        product.setStatus(dto.getStatus());
        productRepository.save(product);

        // Cập nhật hoặc thêm ảnh
        if (dto.getImages() != null) {
            productImageRepository.deleteByProductId(dto.getProductId()); // Xóa ảnh cũ
            List<ProductImage> images = dto.getImages().stream()
                    .map(imageDto -> convertToImageEntity(imageDto, dto.getProductId()))
                    .collect(Collectors.toList());
            productImageRepository.saveAll(images);
        }

        // Cập nhật hoặc thêm biến thể
        if (dto.getVariants() != null) {
            productVariantRepository.deleteByProductId(dto.getProductId()); // Xóa biến thể cũ
            List<ProductVariant> variants = dto.getVariants().stream()
                    .map(variantDto -> convertToVariantEntity(variantDto, dto.getProductId()))
                    .collect(Collectors.toList());
            productVariantRepository.saveAll(variants);
        }
    }

    public void deleteProductWithImagesAndVariants(Integer id) {
        productImageRepository.deleteByProductId(id);
        productVariantRepository.deleteByProductId(id);
        productRepository.deleteById(id);
    }

    private ProductWithImagesAndVariantsDTO convertToDTO(Product product) {
        ProductWithImagesAndVariantsDTO dto = new ProductWithImagesAndVariantsDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setCategoryId(product.getCategoryId());
        dto.setStatus(product.getStatus());
        dto.setImages(productImageRepository.findByProductId(product.getProductId()).stream()
                .map(this::convertToImageDTO).collect(Collectors.toList()));
        dto.setVariants(productVariantRepository.findByProductId(product.getProductId()).stream()
                .map(this::convertToVariantDTO).collect(Collectors.toList()));
        return dto;
    }

    private Product convertToEntity(ProductWithImagesAndVariantsDTO dto) {
        Product product = new Product();
        product.setProductId(dto.getProductId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategoryId(dto.getCategoryId());
        product.setStatus(dto.getStatus());
        return product;
    }

    private ProductImage convertToImageEntity(ProductImageDTO dto, Integer productId) {
        ProductImage image = new ProductImage();
        image.setImageId(dto.getImageId());
        image.setProductId(productId);
        image.setImageUrl(dto.getImageUrl());
        image.setIsPrimary(dto.getIsPrimary());
        return image;
    }

    private ProductImageDTO convertToImageDTO(ProductImage image) {
        ProductImageDTO dto = new ProductImageDTO();
        dto.setImageId(image.getImageId());
        dto.setProductId(image.getProductId());
        dto.setImageUrl(image.getImageUrl());
        dto.setIsPrimary(image.getIsPrimary());
        return dto;
    }

    private ProductVariant convertToVariantEntity(ProductVariantDTO dto, Integer productId) {
        ProductVariant variant = new ProductVariant();
        variant.setVariantId(dto.getVariantId());
        variant.setProductId(productId);
        variant.setSku(dto.getSku());
        variant.setColor(dto.getColor());
        variant.setSize(dto.getSize());
        variant.setPrice(dto.getPrice());
        variant.setStock(dto.getStock());
        return variant;
    }

    private ProductVariantDTO convertToVariantDTO(ProductVariant variant) {
        ProductVariantDTO dto = new ProductVariantDTO();
        dto.setVariantId(variant.getVariantId());
        dto.setProductId(variant.getProductId());
        dto.setSku(variant.getSku());
        dto.setColor(variant.getColor());
        dto.setSize(variant.getSize());
        dto.setPrice(variant.getPrice());
        dto.setStock(variant.getStock());
        return dto;
    }
}