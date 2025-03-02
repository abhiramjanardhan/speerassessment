package com.assessment.speernotes.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {
    @Value("${jwt.secret.key}")
    private String secretKey;

    private final long EXPIRATION_TIME = 86400000; // 1 day

    /**
     * Thid method is used to get the secret key
     *
     * @return SecretKey
     */
    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * This method is used to generate the JWT token
     *
     * @param email
     * @return String
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * This method is used to convert the JWT token to claims object
     *
     * @param token
     * @return Claims
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * This method is used to retrieve the email from the JWT token
     *
     * @param token
     * @return String
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * This method is used to validate the JWT token
     *
     * @param token
     * @return boolean
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
