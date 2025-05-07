package com.example.fashionshopbackend.entity.wishlist;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "WishList")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WishListID")
    private Long wishListId;

    @Column(name = "UserID", nullable = false)
    private Long userId;

    @Column(name = "ProductID", nullable = false)
    private Long productId;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private Instant createdAt;
}