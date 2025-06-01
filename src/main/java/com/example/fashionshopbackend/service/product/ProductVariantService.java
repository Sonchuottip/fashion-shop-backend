package com.example.fashionshopbackend.service.product;

import com.example.fashionshopbackend.dto.product.ProductVariantDTO;
import com.example.fashionshopbackend.entity.product.Product;
import com.example.fashionshopbackend.entity.product.ProductVariant;
import com.example.fashionshopbackend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductVariantService {

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    @Async
    public ProductVariantDTO addProductVariants(Integer productId, List<ProductVariantDTO> newVariants) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        List<ProductVariant> productVariants = product.getVariants();
        productVariants.addAll(newVariants.stream().map(v -> {
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setSku(v.getSku());
            variant.setColor(v.getColor());
            variant.setSize(v.getSize());
            variant.setPrice(v.getOriginalPrice());
            // Không đặt stock mặc định, để trigger hoặc inventory_history xử lý
            variant.setStatus("active");
            return variant;
        }).collect(Collectors.toList()));
        product.setVariants(productVariants);

        productRepository.save(product);
        return convertToVariantDTO(productVariants.get(productVariants.size() - 1));
    }

    @Transactional
    public ProductVariantDTO updateProductVariant(Integer productId, Integer variantId, ProductVariantDTO variantDTO) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getVariantId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại"));

        variant.setSku(variantDTO.getSku());
        variant.setColor(variantDTO.getColor());
        variant.setSize(variantDTO.getSize());
        variant.setPrice(variantDTO.getOriginalPrice());
        variant.setStatus(variantDTO.getStatus());
        // Không cập nhật stock, để inventory_history xử lý

        productRepository.save(product);
        return convertToVariantDTO(variant);
    }

    @Transactional
    public void deleteVariant(Integer productId, Integer variantId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        ProductVariant variantToDelete = product.getVariants().stream()
                .filter(v -> v.getVariantId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại"));

        variantToDelete.setStatus("deleted");
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantDTO> getVariantsByStatus(Integer productId, String statusFilter) {
        List<ProductVariantDTO> variants = productRepository.findById(productId)
                .map(product -> product.getVariants().stream()
                        .map(this::convertToVariantDTO)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        if (statusFilter == null || statusFilter.isEmpty()) {
            return variants.stream()
                    .filter(v -> "active".equals(v.getStatus()))
                    .collect(Collectors.toList());
        } else if (statusFilter.equals("all")) {
            return variants;
        } else {
            return variants.stream()
                    .filter(v -> statusFilter.equals(v.getStatus()))
                    .collect(Collectors.toList());
        }
    }

    private ProductVariantDTO convertToVariantDTO(ProductVariant variant) {
        ProductVariantDTO dto = new ProductVariantDTO();
        dto.setVariantId(variant.getVariantId());
        dto.setSku(variant.getSku());
        dto.setColor(variant.getColor());
        dto.setSize(variant.getSize());
        dto.setOriginalPrice(variant.getPrice());
        dto.setStock(variant.getStock()); // Vẫn trả về stock để hiển thị, nhưng không chỉnh sửa
        dto.setStatus(variant.getStatus());
        return dto;
    }
}