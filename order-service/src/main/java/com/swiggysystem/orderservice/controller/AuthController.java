package com.swiggysystem.orderservice.controller;

import com.swiggysystem.orderservice.dto.LoginRequest;
import com.swiggysystem.orderservice.dto.LoginResponse;
import com.swiggysystem.orderservice.entity.RefreshToken;
import com.swiggysystem.orderservice.repository.RefreshTokenRepository;
import com.swiggysystem.orderservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    // ⚠️ demo ke liye hardcoded user hai - real system mein yeh MySQL 'users' table se
    // aur BCrypt-hashed password check se aayega, plain text compare nahi
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "testpass";
    private static final Long TEST_USER_ID = 1L;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (!TEST_USERNAME.equals(request.getUsername()) || !TEST_PASSWORD.equals(request.getPassword())) {
            return ResponseEntity.status(401).build();   // authentication fail - galat credentials
        }

        String accessToken = jwtUtil.generateAccessToken(TEST_USER_ID);

        // refresh token banao aur DB mein store karo, taaki baad mein revoke kar sakein
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(jwtUtil.generateRefreshTokenValue());
        refreshToken.setUserId(TEST_USER_ID);
        refreshToken.setExpiresAt(Instant.now().plusMillis(jwtUtil.getRefreshTokenExpirationMs()));
        refreshTokenRepository.save(refreshToken);

        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestParam String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken).orElse(null);

        if (stored == null || stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            return ResponseEntity.status(401).build();   // refresh token invalid, expired, ya revoke ho chuka hai
        }

        String newAccessToken = jwtUtil.generateAccessToken(stored.getUserId());
        // point: hum yahan wahi purana refresh token wapas bhej rahe hain - real system mein
        // "refresh token rotation" bhi kar sakte hain (har refresh par naya refresh token bhi issue karo)
        return ResponseEntity.ok(new LoginResponse(newAccessToken, refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevoked(true);              // DB mein revoke mark karo
            refreshTokenRepository.save(token);
        });
        return ResponseEntity.ok().build();
    }
}