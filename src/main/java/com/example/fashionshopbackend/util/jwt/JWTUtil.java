package com.example.fashionshopbackend.util.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Component
public class JWTUtil {

    private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.secret:defaultSecretKey}")
    private String secret;

    @Autowired
    private RSAKey rsaKey;

    public String generateToken(String email, Long userId, String role) throws JOSEException {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(email)
                .claim("userId", userId) // Thêm userId vào token
                .claim("role", role)
                .issuer("fashion-shop-backend")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + expiration * 1000))
                .build();

        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        JWSObject jwsObject = new JWSObject(jwsHeader, new Payload(claimsSet.toJSONObject()));
        JWSSigner signer = new MACSigner(secret.getBytes());
        jwsObject.sign(signer);

        JWEHeader jweHeader = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                .contentType("JWT")
                .build();

        JWEObject jweObject = new JWEObject(jweHeader, new Payload(jwsObject));
        jweObject.encrypt(new RSAEncrypter(rsaKey));

        String token = jweObject.serialize();
        logger.debug("Generated JWE token: {}", token);
        return token;
    }

    public String getEmailFromToken(String token) throws ParseException, JOSEException {
        JWEObject jweObject = JWEObject.parse(token);
        jweObject.decrypt(new RSADecrypter(rsaKey));
        SignedJWT signedJWT = SignedJWT.parse(jweObject.getPayload().toString());
        return signedJWT.getJWTClaimsSet().getSubject();
    }

    public Long getUserIdFromToken(String token) throws ParseException, JOSEException {
        JWEObject jweObject = JWEObject.parse(token);
        jweObject.decrypt(new RSADecrypter(rsaKey));
        SignedJWT signedJWT = SignedJWT.parse(jweObject.getPayload().toString());
        return (Long) signedJWT.getJWTClaimsSet().getClaim("userId");
    }

    public boolean validateToken(String token, UserDetails userDetails) throws ParseException, JOSEException {
        JWEObject jweObject = JWEObject.parse(token);
        jweObject.decrypt(new RSADecrypter(rsaKey));
        SignedJWT signedJWT = SignedJWT.parse(jweObject.getPayload().toString());
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

        String email = claimsSet.getSubject();
        if (!email.equals(userDetails.getUsername())) {
            return false;
        }

        Date expirationTime = claimsSet.getExpirationTime();
        return expirationTime != null && !expirationTime.before(new Date());
    }
}