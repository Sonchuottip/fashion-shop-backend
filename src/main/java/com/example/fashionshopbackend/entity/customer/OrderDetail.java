package com.example.fashionshopbackend.entity.customer;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Order_Details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderDetailID")
    private Long orderDetailId;

    @Column(name = "OrderID", nullable = false)
    private Long orderId;

    @Column(name = "VariantID", nullable = false)
    private Long variantId;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "Price", nullable = false)
    private Double price;
}