package com.example.fashionshopbackend.service.admin;

import com.example.fashionshopbackend.dto.product.CategoryDTO;
import com.example.fashionshopbackend.dto.product.ProductImageDTO;
import com.example.fashionshopbackend.dto.product.ProductVariantDTO;
import com.example.fashionshopbackend.dto.product.ProductWithImagesAndVariantsDTO;
import com.example.fashionshopbackend.dto.product.ProductDTO;
import com.example.fashionshopbackend.entity.product.Product;
import com.example.fashionshopbackend.entity.product.ProductImage;
import com.example.fashionshopbackend.entity.product.ProductVariant;
import com.example.fashionshopbackend.repository.product.ProductImageRepository;
import com.example.fashionshopbackend.repository.product.ProductRepository;
import com.example.fashionshopbackend.repository.product.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CategoryService categoryService;

    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::convertToProductDTO).collect(Collectors.toList());
    }

    public ProductWithImagesAndVariantsDTO getProductWithImagesAndVariantsById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));
        return convertToDTO(product);
    }

    public List<ProductWithImagesAndVariantsDTO> searchProducts(String query) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(query);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryService.getAllCategories();
    }

    public List<ProductWithImagesAndVariantsDTO> getProductsByCategoryId(Integer categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void createProductWithImagesAndVariants(ProductWithImagesAndVariantsDTO dto) {
        // Chuyển DTO thành entity Product và lưu
        Product product = convertToEntity(dto);
        product = productRepository.save(product);
        final Integer productId = product.getProductId();

        // Xử lý hình ảnh
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<ProductImage> images = dto.getImages().stream()
                    .map(imageDto -> {
                        ProductImage image = convertToImageEntity(imageDto, productId);
                        if (imageDto.getFile() != null && !imageDto.getFile().isEmpty()) {
                            try {
                                // Lưu file vào thư mục static/image
                                String uploadDir = "src/main/resources/static/images/";
                                String fileName = System.currentTimeMillis() + "_" + imageDto.getFile().getOriginalFilename();
                                Path filePath = Paths.get(uploadDir + fileName);
                                Files.createDirectories(filePath.getParent());
                                Files.write(filePath, imageDto.getFile().getBytes());

                                // Cập nhật imageUrl với đường dẫn tương đối
                                image.setImageUrl("/images/" + fileName);
                            } catch (IOException e) {
                                throw new RuntimeException("Lỗi khi lưu hình ảnh: " + e.getMessage(), e);
                            }
                        }
                        return image;
                    })
                    .collect(Collectors.toList());
            productImageRepository.saveAll(images);
        }

        // Xử lý biến thể
        if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
            List<ProductVariant> variants = dto.getVariants().stream()
                    .map(variantDto -> convertToVariantEntity(variantDto, productId))
                    .collect(Collectors.toList());
            productVariantRepository.saveAll(variants);
        }
    }

    @Transactional
    public void updateProductWithImagesAndVariants(ProductWithImagesAndVariantsDTO dto) {
        // Tìm sản phẩm
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + dto.getProductId()));

        // Cập nhật thông tin sản phẩm
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategoryId(dto.getCategoryId());
        product.setStatus(dto.getStatus());
        productRepository.save(product);

        // Xử lý hình ảnh
        if (dto.getImages() != null) {
            // Xóa hình ảnh cũ và tệp liên quan
            List<ProductImage> oldImages = productImageRepository.findByProductId(dto.getProductId());
            for (ProductImage oldImage : oldImages) {
                try {
                    if (oldImage.getImageUrl() != null) {
                        Path oldFilePath = Paths.get("src/main/resources/static" + oldImage.getImageUrl());
                        Files.deleteIfExists(oldFilePath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Lỗi khi xóa hình ảnh cũ: " + e.getMessage(), e);
                }
            }
            productImageRepository.deleteByProductId(dto.getProductId());

            // Lưu hình ảnh mới
            if (!dto.getImages().isEmpty()) {
                List<ProductImage> images = dto.getImages().stream()
                        .map(imageDto -> {
                            ProductImage image = convertToImageEntity(imageDto, dto.getProductId());
                            if (imageDto.getFile() != null && !imageDto.getFile().isEmpty()) {
                                try {
                                    // Lưu file mới
                                    String uploadDir = "src/main/resources/static/images/";
                                    String fileName = System.currentTimeMillis() + "_" + imageDto.getFile().getOriginalFilename();
                                    Path filePath = Paths.get(uploadDir + fileName);
                                    Files.createDirectories(filePath.getParent());
                                    Files.write(filePath, imageDto.getFile().getBytes());
                                    image.setImageUrl("/images/" + fileName);
                                } catch (IOException e) {
                                    throw new RuntimeException("Lỗi khi lưu hình ảnh: " + e.getMessage(), e);
                                }
                            }
                            return image;
                        })
                        .collect(Collectors.toList());
                productImageRepository.saveAll(images);
            }
        }

        // Xử lý biến thể
        if (dto.getVariants() != null) {
            productVariantRepository.deleteByProductId(dto.getProductId());
            if (!dto.getVariants().isEmpty()) {
                List<ProductVariant> variants = dto.getVariants().stream()
                        .map(variantDto -> convertToVariantEntity(variantDto, dto.getProductId()))
                        .collect(Collectors.toList());
                productVariantRepository.saveAll(variants);
            }
        }
    }

    @Transactional
    public void deleteProductWithImagesAndVariants(Integer id) {
        // Kiểm tra sản phẩm có tồn tại
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));

        // Xóa hình ảnh và tệp liên quan
        List<ProductImage> images = productImageRepository.findByProductId(id);
        for (ProductImage image : images) {
            try {
                if (image.getImageUrl() != null) {
                    Path filePath = Paths.get("src/main/resources/static" + image.getImageUrl());
                    Files.deleteIfExists(filePath);
                }
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi xóa tệp hình ảnh: " + e.getMessage(), e);
            }
        }
        productImageRepository.deleteByProductId(id);

        // Xóa biến thể
        productVariantRepository.deleteByProductId(id);

        // Xóa sản phẩm
        productRepository.deleteById(id);
    }

    private ProductDTO convertToProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setCategoryId(product.getCategoryId());
        dto.setStatus(product.getStatus());

        // Lấy ảnh chính (isPrimary = true) cho sản phẩm
        Optional<ProductImage> primaryImage = productImageRepository.findByProductIdAndIsPrimary(product.getProductId(), true);
        primaryImage.ifPresent(image -> dto.setImageUrl(image.getImageUrl()));

        return dto;
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