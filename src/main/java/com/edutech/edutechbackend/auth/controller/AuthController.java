package com.edutech.edutechbackend.auth.controller;

import com.edutech.edutechbackend.auth.dto.UserLoginRequest;
import com.edutech.edutechbackend.auth.dto.UserRegisterRequest;
import com.edutech.edutechbackend.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

/**
 * Production-hardened AuthController
 * ─────────────────────────────────────────────────────────────────────────────
 * Changes from dev version:
 *  1. Uses Spring's ResponseCookie builder instead of raw Set-Cookie string
 *     → ResponseCookie handles escaping and attribute formatting correctly
 *  2. Cookie "secure" flag is driven by a config property so local HTTP dev works
 *  3. SameSite=None is required for cross-origin Vercel → Render setup
 *  4. Consistent cookie clearing on logout (same path/domain as issue)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Set to false in dev profile (HTTP localhost) via application-dev.yml.
     * Always true in prod (HTTPS on Render/Vercel).
     */
    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequest request) {
        authService.register(request);
        return ResponseEntity
                .status(201)
                .body(Map.of("message", "Account created successfully."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletResponse response
    ) {
        String token = authService.login(request);
        response.addHeader(HttpHeaders.SET_COOKIE, buildJwtCookie(token).toString());
        return ResponseEntity.ok(Map.of("message", "Login successful."));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, clearJwtCookie().toString());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    // ── Cookie helpers ────────────────────────────────────────────────────────

    private ResponseCookie buildJwtCookie(String token) {
        return ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofMillis(jwtExpirationMs))
                .sameSite("None")   // Required for cross-origin (Vercel → Render)
                .build();
    }

    private ResponseCookie clearJwtCookie() {
        return ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ZERO)
                .sameSite("None")
                .build();
    }
}