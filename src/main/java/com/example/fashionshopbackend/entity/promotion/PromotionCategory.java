package com.example.fashionshopbackend.entity.promotion;

import com.example.fashionshopbackend.entity.product.Category;
import jakarta.persistence.*;

import lombok.Data;

@Entity
@Table(name = "promotion_categories")
@Data
public class PromotionCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer promotionCategoryId;

    @Column(name = "promotion_id", nullable = false)
    private Integer promotionId;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @ManyToOne
    @JoinColumn(name = "promotion_id", insertable = false, updatable = false)
    private Promotion promotion;

    @ManyToOne
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;
}