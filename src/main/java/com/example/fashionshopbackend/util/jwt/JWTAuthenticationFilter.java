package com.example.fashionshopbackend.util.jwt;

import com.example.fashionshopbackend.service.auth.CustomUserDetailsService;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    @Autowired
    @Lazy // Trì hoãn khởi tạo JWTUtil
    private JWTUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private RSAKey rsaKey; // Inject RSAKey từ RsaKeyConfig

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        logger.debug("Processing request URI: {}", requestURI);

        // Bỏ qua các endpoint không yêu cầu token
        if (requestURI.equals("/api/auth/login") ||
                requestURI.equals("/api/auth/register") ||
                requestURI.equals("/api/auth/forgot-password") ||
                requestURI.equals("/api/auth/reset-password")) {
            logger.debug("Skipping JWT authentication for URI: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String email = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                // Parse and decrypt JWE token
                JWEObject jweObject = JWEObject.parse(jwt);
                logger.debug("JWE Header: {}", jweObject.getHeader().toString());

                // Decrypt token
                jweObject.decrypt(new RSADecrypter(rsaKey));
                String payload = jweObject.getPayload().toString();
                logger.debug("Decrypted JWE Payload: {}", payload);

                // Extract email from decrypted payload using JWTUtil
                email = jwtUtil.getEmailFromToken(jwt); // Truyền toàn bộ token
                logger.debug("Extracted email from token: {}", email);
            } catch (Exception e) {
                logger.error("Invalid JWE token: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or malformed token");
                return;
            }
        } else {
            logger.debug("No Bearer token found in Authorization header");
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            try {
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication set for user: {}", email);
                } else {
                    logger.debug("Token validation failed for user: {}", email);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token validation failed");
                    return;
                }
            } catch (Exception e) {
                logger.error("Token validation error: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token validation failed");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}