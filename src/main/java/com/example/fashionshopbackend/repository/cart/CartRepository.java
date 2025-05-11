package com.example.fashionshopbackend.repository.cart;

import com.example.fashionshopbackend.entity.customer.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}