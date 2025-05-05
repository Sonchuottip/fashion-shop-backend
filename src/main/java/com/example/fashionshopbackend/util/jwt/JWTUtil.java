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

    public String generateToken(String email, String role) throws JOSEException {
        // Create JWT claims
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(email)
                .claim("role", role)
                .issuer("fashion-shop-backend")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + expiration * 1000))
                .build();

        // Create JWS header
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);

        // Create JWS object
        JWSObject jwsObject = new JWSObject(jwsHeader, new Payload(claimsSet.toJSONObject()));

        // Ký JWS với thuật toán HS256 và khóa bí mật
        JWSSigner signer = new MACSigner(secret.getBytes());
        jwsObject.sign(signer);

        // Create JWE header
        JWEHeader jweHeader = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                .contentType("JWT")
                .build();

        // Create JWE object với JWS đã ký làm payload
        JWEObject jweObject = new JWEObject(jweHeader, new Payload(jwsObject));

        // Encrypt the JWE
        jweObject.encrypt(new RSAEncrypter(rsaKey));

        // Serialize to string
        String token = jweObject.serialize();
        logger.debug("Generated JWE token: {}", token);
        return token;
    }

    public String getEmailFromToken(String token) throws ParseException, JOSEException {
        // Parse and decrypt JWE token
        JWEObject jweObject = JWEObject.parse(token);
        jweObject.decrypt(new RSADecrypter(rsaKey));

        // Get the payload (which is a JWS)
        SignedJWT signedJWT = SignedJWT.parse(jweObject.getPayload().toString());
        return signedJWT.getJWTClaimsSet().getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) throws ParseException, JOSEException {
        // Parse and decrypt JWE token
        JWEObject jweObject = JWEObject.parse(token);
        jweObject.decrypt(new RSADecrypter(rsaKey));

        // Get the payload (which is a JWS)
        SignedJWT signedJWT = SignedJWT.parse(jweObject.getPayload().toString());
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

        // Validate email
        String email = claimsSet.getSubject();
        if (!email.equals(userDetails.getUsername())) {
            return false;
        }

        // Validate expiration
        Date expirationTime = claimsSet.getExpirationTime();
        return expirationTime != null && !expirationTime.before(new Date());
    }
}