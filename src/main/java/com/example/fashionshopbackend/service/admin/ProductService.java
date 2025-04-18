package com.example.fashionshopbackend.service.admin;

import com.example.fashionshopbackend.dto.product.ProductDTO;
import com.example.fashionshopbackend.dto.product.ProductImageDTO;
import com.example.fashionshopbackend.dto.product.ProductVariantDTO;
import com.example.fashionshopbackend.dto.product.ProductWithImagesDTO;
import com.example.fashionshopbackend.entity.category.Category;
import com.example.fashionshopbackend.entity.product.Product;
import com.example.fashionshopbackend.entity.product.ProductImage;
import com.example.fashionshopbackend.entity.product.ProductVariant;
import com.example.fashionshopbackend.repository.category.CategoryRepository;
import com.example.fashionshopbackend.repository.product.ProductImageRepository;
import com.example.fashionshopbackend.repository.product.ProductRepository;
import com.example.fashionshopbackend.repository.product.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Transactional
    public Product createProduct(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(BigDecimal.valueOf(dto.getPrice()));
        product.setStock(dto.getStock());
        if (dto.getStatus() != null) {
            product.setStatus(dto.getStatus());
        }

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + dto.getCategoryId()));
            product.setCategory(category);
        }

        return productRepository.save(product);
    }

    @Transactional
    public Product createProductWithImages(ProductWithImagesDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(BigDecimal.valueOf(dto.getPrice()));
        product.setStock(dto.getStock());
        if (dto.getStatus() != null) {
            product.setStatus(dto.getStatus());
        }

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + dto.getCategoryId()));
            product.setCategory(category);
        }

        product = productRepository.save(product);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (ProductImageDTO imageDTO : dto.getImages()) {
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setImageUrl(imageDTO.getImageUrl());
                image.setIsPrimary(imageDTO.getIsPrimary() != null ? imageDTO.getIsPrimary() : false);
                productImageRepository.save(image);
            }
        }

        return product;
    }

    @Transactional
    public Product updateProduct(Integer id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(BigDecimal.valueOf(dto.getPrice()));
        product.setStock(dto.getStock());
        if (dto.getStatus() != null) {
            product.setStatus(dto.getStatus());
        }

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + dto.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Integer id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public ProductImage createProductImage(ProductImageDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + dto.getProductId()));

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(dto.getImageUrl());
        image.setIsPrimary(dto.getIsPrimary() != null ? dto.getIsPrimary() : false);
        return productImageRepository.save(image);
    }

    @Transactional
    public ProductImage updateProductImage(Integer id, ProductImageDTO dto) {
        ProductImage image = productImageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product image not found with ID: " + id));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + dto.getProductId()));

        image.setProduct(product);
        image.setImageUrl(dto.getImageUrl());
        image.setIsPrimary(dto.getIsPrimary() != null ? dto.getIsPrimary() : image.getIsPrimary());
        return productImageRepository.save(image);
    }

    @Transactional
    public void deleteProductImage(Integer id) {
        if (!productImageRepository.existsById(id)) {
            throw new IllegalArgumentException("Product image not found with ID: " + id);
        }
        productImageRepository.deleteById(id);
    }

    @Transactional
    public ProductVariant createProductVariant(ProductVariantDTO dto) {
        if (productVariantRepository.existsBySku(dto.getSku())) {
            throw new IllegalArgumentException("SKU already exists");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + dto.getProductId()));

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(dto.getSku());
        variant.setColor(dto.getColor());
        variant.setSize(dto.getSize());
        variant.setPrice(BigDecimal.valueOf(dto.getPrice()));
        variant.setStock(dto.getStock());
        return productVariantRepository.save(variant);
    }

    @Transactional
    public ProductVariant updateProductVariant(Integer id, ProductVariantDTO dto) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product variant not found with ID: " + id));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + dto.getProductId()));

        if (!variant.getSku().equals(dto.getSku()) && productVariantRepository.existsBySku(dto.getSku())) {
            throw new IllegalArgumentException("SKU already exists");
        }

        variant.setProduct(product);
        variant.setSku(dto.getSku());
        variant.setColor(dto.getColor());
        variant.setSize(dto.getSize());
        variant.setPrice(BigDecimal.valueOf(dto.getPrice()));
        variant.setStock(dto.getStock());
        return productVariantRepository.save(variant);
    }

    @Transactional
    public void deleteProductVariant(Integer id) {
        if (!productVariantRepository.existsById(id)) {
            throw new IllegalArgumentException("Product variant not found with ID: " + id);
        }
        productVariantRepository.deleteById(id);
    }
}