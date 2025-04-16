package com.example.fashionshopbackend.config;

import com.example.fashionshopbackend.entity.User;
import com.example.fashionshopbackend.repository.UserRepository;
import com.example.fashionshopbackend.service.CustomUserDetailsService;
import com.example.fashionshopbackend.util.JWTAuthenticationFilter;
import com.example.fashionshopbackend.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
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
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/login.html").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("Admin")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login.html")
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService())
                                .userService(oauth2UserService())
                        )
                        .defaultSuccessUrl("/api/auth/oauth2/success", true)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return (userRequest) -> {
            OAuth2User oauth2User = delegate.loadUser(userRequest);
            String provider = userRequest.getClientRegistration().getRegistrationId(); // "google" hoặc "facebook"
            String email = oauth2User.getAttribute("email");
            String fullName = oauth2User.getAttribute("name");
            String providerId = oauth2User.getName(); // ID từ Google/Facebook

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setFullName(fullName);
                newUser.setProvider(provider);
                newUser.setProviderId(providerId);
                newUser.setRole("Customer");
                return userRepository.save(newUser);
            });

            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    "",
                    userDetailsService.loadUserByUsername(user.getEmail()).getAuthorities()
            );
        };
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcUserService delegate = new OidcUserService();
        return (userRequest) -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);
            String provider = userRequest.getClientRegistration().getRegistrationId();
            String email = oidcUser.getEmail();
            String fullName = oidcUser.getFullName();
            String providerId = oidcUser.getSubject();

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setFullName(fullName);
                newUser.setProvider(provider);
                newUser.setProviderId(providerId);
                newUser.setRole("Customer");
                return userRepository.save(newUser);
            });

            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    "",
                    userDetailsService.loadUserByUsername(user.getEmail()).getAuthorities()
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