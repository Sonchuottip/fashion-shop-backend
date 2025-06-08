package com.example.fashionshopbackend.util;

import com.example.fashionshopbackend.service.auth.TokenService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Component
@Getter
public class JWTUtil {

    private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private TokenService tokenService;

    public String generateToken(Long userId, String email, String role) throws JOSEException {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .claim("type", "access")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + expiration * 1000))
                .build();

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(new MACSigner(secret));

        String token = signedJWT.serialize();
        logger.debug("Generated access token for user {}: {}", userId, token);

        tokenService.saveToken(userId.toString(), token, expiration * 1000);
        return token;
    }

    public String generateRefreshToken(Long userId) throws JOSEException {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 604800 * 1000)) // 7 ngày
                .build();

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(new MACSigner(secret));

        String token = signedJWT.serialize();
        logger.debug("Generated refresh token for user {}: {}", userId, token);
        return token;
    }

    public String getEmailFromToken(String token) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        if (!signedJWT.verify(new MACVerifier(secret))) {
            throw new JOSEException("Chữ ký không hợp lệ");
        }
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        return claimsSet.getSubject();
    }

    public long getUserIdFromToken(String token) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        if (!signedJWT.verify(new MACVerifier(secret))) {
            throw new JOSEException("Chữ ký không hợp lệ");
        }
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        return claimsSet.getLongClaim("userId");
    }

    public String getRoleFromToken(String token) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        if (!signedJWT.verify(new MACVerifier(secret))) {
            throw new JOSEException("Chữ ký không hợp lệ");
        }
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        return claimsSet.getStringClaim("role");
    }

    public boolean validateToken(String token, UserDetails userDetails) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        if (!signedJWT.verify(new MACVerifier(secret))) {
            logger.warn("Chữ ký token không hợp lệ");
            return false;
        }

        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        String email = claimsSet.getSubject();
        long userId = claimsSet.getLongClaim("userId");

        if (!email.equals(userDetails.getUsername())) {
            logger.warn("Email không khớp: token email={}, userDetails username={}", email, userDetails.getUsername());
            return false;
        }

        if (tokenService.isSpecificTokenRevoke(String.valueOf(userId), token)) {
            logger.warn("Token cụ thể cho người dùng {} đã bị thu hồi", userId);
            return false;
        }

        Date expirationDate = claimsSet.getExpirationTime();
        if (expirationDate == null || expirationDate.before(new Date())) {
            logger.warn("Token cho người dùng {} đã hết hạn hoặc không có thời gian hết hạn", userId);
            return false;
        }
        return true;
    }

    public boolean validateRefreshToken(String token, Long userId) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        if (!signedJWT.verify(new MACVerifier(secret))) {
            logger.warn("Chữ ký refresh token không hợp lệ");
            return false;
        }

        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        String tokenType = claimsSet.getStringClaim("type");
        String subject = claimsSet.getSubject();

        if (!"refresh".equals(tokenType)) {
            logger.warn("Token không phải refresh token");
            return false;
        }

        if (!subject.equals(userId.toString())) {
            logger.warn("UserId không khớp trong refresh token: token userId={}, expected={}", subject, userId);
            return false;
        }

        Date expirationDate = claimsSet.getExpirationTime();
        if (expirationDate == null || expirationDate.before(new Date())) {
            logger.warn("Refresh token đã hết hạn");
            return false;
        }

        return true;
    }

    public boolean isTokenExpired(String token) throws JOSEException, ParseException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(new MACVerifier(secret))) {
                logger.warn("Chữ ký token không hợp lệ");
                return true;
            }
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            Date expirationDate = claimsSet.getExpirationTime();
            if (expirationDate == null) {
                logger.warn("Token không chứa thời gian hết hạn");
                return true;
            }
            boolean isExpired = expirationDate.before(new Date());
            if (isExpired) {
                logger.debug("Token hết hạn vào: {}", expirationDate);
            }
            return isExpired;
        } catch (JOSEException | ParseException e) {
            logger.error("Lỗi khi kiểm tra thời hạn token: {}", e.getMessage());
            throw e;
        }
    }

    public void revokeToken(String userId) {
        tokenService.revokeToken(userId);
        logger.debug("Thu hồi token cho người dùng {}", userId);
    }
}