package com.example.fashionshopbackend.config;

import com.example.fashionshopbackend.repository.user.UserRepository;
import com.example.fashionshopbackend.service.auth.CustomUserDetailsService;
import com.example.fashionshopbackend.util.jwt.JWTAuthenticationFilter;
import com.example.fashionshopbackend.util.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private JWTAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Define các endpoint công khai không yêu cầu xác thực
        RequestMatcher permitAllMatcher = new AntPathRequestMatcher("/api/auth/{register|login|forgot-password|reset-password|oauth2/**}");

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Chỉ cho phép công khai các endpoint cần thiết
                        .requestMatchers("/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/oauth2/**").permitAll()
                        // Yêu cầu xác thực cho các endpoint khác trong /api/auth/**
                        .requestMatchers("/api/auth/logout",
                                "/api/auth/profile",
                                "/api/auth/profile/update",
                                "/api/auth/change-password").authenticated()
                        // Yêu cầu vai trò Admin cho các endpoint admin
                        .requestMatchers("/api/admin/**").hasAuthority("Admin") // Sử dụng hasAuthority thay vì hasRole
                        // Tất cả các yêu cầu khác phải được xác thực
                        .anyRequest().authenticated()
                )
                // Thêm hỗ trợ OAuth2 login
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/api/auth/oauth2/callback", true)
                )
                // Xử lý ngoại lệ xác thực
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (permitAllMatcher.matches(request)) {
                                // Nếu là endpoint permitAll(), không trả về lỗi Unauthorized
                                return;
                            }
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Bạn cần đăng nhập để truy cập tài nguyên này\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Bạn không có quyền truy cập tài nguyên này\"}");
                        })
                )
                // Sử dụng stateless session vì dùng JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Thêm bộ lọc JWT trước UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}