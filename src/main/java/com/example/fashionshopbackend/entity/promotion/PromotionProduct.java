package com.example.fashionshopbackend.entity.promotion;

import jakarta.persistence.*;
import com.example.fashionshopbackend.entity.product.Product;
import lombok.Data;

@Entity
@Table(name = "promotion_products")
@Data
public class PromotionProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer promotionProductId;

    @Column(name = "promotion_id", nullable = false)
    private Integer promotionId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @ManyToOne
    @JoinColumn(name = "promotion_id", insertable = false, updatable = false)
    private Promotion promotion;

    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
}