package com.swiggysystem.orderservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey key = Keys.hmacShaKeyFor("this-is-a-demo-secret-key-change-in-real-system-32bytes".getBytes());

    private static final long ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000;      // 15 minutes - short-lived by design
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7L * 24 * 60 * 60 * 1000; // 7 days

    public String generateAccessToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_MS))
                .signWith(key)
                .compact();
    }

    // refresh token sirf ek random opaque string hai, JWT nahi - DB mein store hoga
    public String generateRefreshTokenValue() {
        return UUID.randomUUID().toString();
    }

    public long getRefreshTokenExpirationMs() {
        return REFRESH_TOKEN_EXPIRATION_MS;
    }

    public Long validateAndExtractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }
}