package com.example.fashionshopbackend.service;

import com.example.fashionshopbackend.entity.User;
import com.example.fashionshopbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        if (user.getPasswordHash() == null) {
            throw new IllegalStateException("Password hash cannot be null for user: " + email);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPasswordHash(), new ArrayList<>());
    }
}