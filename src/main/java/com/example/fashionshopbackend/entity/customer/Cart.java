package com.example.fashionshopbackend.entity.customer;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Cart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CartID")
    private Long cartId;

    @Column(name = "UserID", nullable = false)
    private Long userId;

    @Column(name = "VariantID", nullable = false)
    private Long variantId;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;
}