package com.example.fashionshopbackend.service.auth;

import com.example.fashionshopbackend.entity.auth.RefreshToken;
import com.example.fashionshopbackend.repository.RefreshTokenRepository;
import com.example.fashionshopbackend.util.JWTUtil;
import com.nimbusds.jose.JOSEException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class RefreshTokenService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final long REFRESH_TOKEN_EXPIRY_SECONDS = 604800; // 7 ngày

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Transactional
    public String createRefreshToken(Long userId) throws JOSEException {
        String token = jwtUtil.generateRefreshToken(userId);
        OffsetDateTime expiryDate = OffsetDateTime.now().plus(REFRESH_TOKEN_EXPIRY_SECONDS, ChronoUnit.SECONDS);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiryDate(expiryDate)
                .createdAt(OffsetDateTime.now())
                .build();

        refreshTokenRepository.save(refreshToken);
        logger.info("Created refresh token for userId: {}", userId, token);
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void revokeRefreshToken(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        logger.info("Revoked all refresh tokens for userId: {}", userId);
    }

    @Transactional
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(OffsetDateTime.now());
        logger.info("Cleaned expired refresh tokens");
    }

    public boolean validateRefreshToken(String token) throws JOSEException, ParseException {
        Optional<RefreshToken> refreshTokenOpt = findByToken(token);
        if (refreshTokenOpt.isEmpty()) {
            logger.warn("Refresh token not found: {}", token);
            return false;
        }

        RefreshToken refreshToken = refreshTokenOpt.get();
        if (refreshToken.getExpiryDate().isBefore(OffsetDateTime.now())) {
            logger.warn("Refresh token expired: {}", token);
            return false;
        }

        return jwtUtil.validateRefreshToken(token, refreshToken.getUserId());
    }
}