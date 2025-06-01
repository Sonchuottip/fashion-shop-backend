package com.example.fashionshopbackend.config;

import com.example.fashionshopbackend.entity.auth.User;
import com.example.fashionshopbackend.entity.auth.UserProfile;
import com.example.fashionshopbackend.repository.UserProfileRepository;
import com.example.fashionshopbackend.repository.UserRepository;
import com.example.fashionshopbackend.service.auth.CustomUserDetailsService;
import com.example.fashionshopbackend.util.JWTAuthenticationFilter;
import com.example.fashionshopbackend.util.JWTUtil;
import com.nimbusds.jose.JOSEException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private JWTAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**",
                                "/login/oauth2/code/**",
                                "/error",
                                "/favicon.ico",
                                "/.well-known/**",
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/store/**"
                        ).permitAll()
                        .requestMatchers("/api/contact/**",
                                "/api/products/**",
                                "/api/reviews/**",
                                "/api/coupons/**").permitAll()
                        .requestMatchers("/api/auth/logout",
                                "/api/personal/**",
                                "/api/auth/change-password").authenticated()
                        .requestMatchers("/api/admin/**").hasAuthority("admin")
                        .requestMatchers("/api/customer/**").hasAuthority("customer")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService()))
                        .successHandler((request, response, authentication) -> {
                            logger.info("OAuth2 login successful for user: {}", authentication.getName());
                            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                            String email = oauth2User.getAttribute("email");

                            // Lấy provider từ OAuth2AuthenticationToken
                            String provider = ((org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication)
                                    .getAuthorizedClientRegistrationId();
                            String providerId = provider.equals("google") ? oauth2User.getAttribute("sub") : oauth2User.getAttribute("id");

                            if (email == null) {
                                logger.error("Email not found in OAuth2 user attributes for provider: {}", provider);
                                throw new IllegalStateException("Email not found in OAuth2 user attributes");
                            }

                            // Tạo người dùng nếu chưa tồn tại
                            User user = userRepository.findByEmail(email).orElseGet(() -> {
                                logger.info("Creating new user with email: {} for provider: {}", email, provider);
                                User newUser = new User();
                                newUser.setEmail(email);
                                newUser.setPasswordHash(passwordEncoder().encode(UUID.randomUUID().toString()));
                                newUser.setProvider(provider);
                                newUser.setProviderId(providerId);
                                newUser.setRole("customer");
                                userRepository.save(newUser);

                                UserProfile userProfile = new UserProfile();
                                userProfile.setAvatarUrl(oauth2User.getAttribute("picture"));
                                userProfile.setUserId(newUser.getId());
                                userProfile.setFullName(oauth2User.getAttribute("name"));
                                userProfileRepository.save(userProfile);

                                logger.info("Saved user: {}", newUser.getId());
                                logger.info("Saved profile: {}", userProfile.getUserId());

                                return newUser;
                            });

                            String token = null;
                            try {
                                token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
                            } catch (JOSEException e) {
                                throw new RuntimeException(e);
                            }
                            response.setContentType("application/json");
                            response.getWriter().write("{\"token\": \"" + token + "\", \"user\": {\"email\": \"" + user.getEmail() + "\", \"role\": \"" + user.getRole() + "\"}}");
                        })
                        .failureHandler((request, response, exception) -> {
                            logger.error("OAuth2 authentication failed: {}", exception.getMessage(), exception);
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"OAuth2 authentication failed: " + exception.getMessage() + "\"}");
                        })
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));


        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return (userRequest) -> {
            logger.info("Processing OAuth2 callback for provider: {}", userRequest.getClientRegistration().getRegistrationId());
            OAuth2User oauth2User = delegate.loadUser(userRequest);
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String providerId = registrationId.equals("google") ? oauth2User.getAttribute("sub") : oauth2User.getAttribute("id");

            if (email == null) {
                logger.error("Email not provided by {}", registrationId);
                throw new IllegalArgumentException("Email not provided by " + registrationId);
            }

            logger.info("OAuth2 user attributes: email={}, name={}, providerId={}", email, name, providerId);

            return new DefaultOAuth2User(
                    Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("customer")),
                    oauth2User.getAttributes(),
                    "email"
            );
        };
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