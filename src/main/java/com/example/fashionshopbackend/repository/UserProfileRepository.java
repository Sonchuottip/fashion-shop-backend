package com.example.fashionshopbackend.repository;

import com.example.fashionshopbackend.entity.auth.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
    Optional<UserProfile> findByUserId(Long userId);

}
