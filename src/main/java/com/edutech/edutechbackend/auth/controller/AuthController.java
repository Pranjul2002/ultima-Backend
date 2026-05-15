package com.edutech.edutechbackend.auth.controller;

import com.edutech.edutechbackend.auth.dto.UserLoginRequest;
import com.edutech.edutechbackend.auth.dto.UserRegisterRequest;
import com.edutech.edutechbackend.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * Auth via HttpOnly, SameSite=None, Secure cookie.
 *
 * Why HttpOnly cookies (not localStorage / Bearer header):
 *   - JavaScript cannot read an HttpOnly cookie → XSS cannot steal the token.
 *   - SameSite=None;Secure is required for cross-origin deployments
 *     (Vercel frontend ↔ Render backend).
 *   - The browser sends the cookie automatically on every request, so the
 *     frontend never needs to touch or store the token.
 *
 * Cookie attributes set on every token cookie:
 *   HttpOnly  — JS cannot read it
 *   Secure    — HTTPS only (disabled in dev via COOKIE_SECURE=false)
 *   SameSite=None — required for cross-origin credentialed requests
 *   Path=/    — sent on every request to this domain
 *   Max-Age   — matches JWT expiry (default 24 h)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    static final String COOKIE_NAME = "auth_token";

    private final AuthService authService;

    /**
     * Set to "true" in production (HTTPS).
     * Set to "false" locally so the cookie works over plain HTTP.
     * Env var: COOKIE_SECURE   default: true
     */
    @Value("${cookie.secure:true}")
    private boolean cookieSecure;

    /**
     * Cookie max-age in seconds. Should match jwt.expiration (ms) / 1000.
     * Env var: COOKIE_MAX_AGE   default: 86400 (24 h)
     */
    @Value("${cookie.max-age:86400}")
    private int cookieMaxAge;

    // ── Register ──────────────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequest request) {
        authService.register(request);
        return ResponseEntity
                .status(201)
                .body(Map.of("message", "Account created successfully."));
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletResponse response
    ) {
        String token = authService.login(request);
        addTokenCookie(response, token);
        return ResponseEntity.ok(Map.of("message", "Login successful."));
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        clearTokenCookie(response);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    // ── Token check (used by frontend on page load) ───────────────────────────

    /**
     * The frontend calls GET /api/auth/me on every page load to check whether
     * a valid session cookie is present. Returns 200 + basic user info if valid,
     * 401 if not (handled by JwtAuthFilter before reaching this method).
     *
     * This replaces the old pattern of storing the token in localStorage and
     * re-validating it client-side.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        // JwtAuthFilter already validated the cookie and set SecurityContext.
        // We delegate to /api/user/profile for the actual profile data;
        // this endpoint simply confirms the cookie is valid (returns 200).
        return ResponseEntity.ok(Map.of("authenticated", true));
    }

    // ── Cookie helpers ────────────────────────────────────────────────────────

    private void addTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(cookieMaxAge);
        // SameSite=None is required for cross-origin (different domain) requests.
        // Jakarta Servlet API < 6 has no setComment/setAttribute for SameSite,
        // so we write the Set-Cookie header manually to include it.
        response.addCookie(cookie);
        // Overwrite with a proper SameSite attribute appended
        String headerValue = buildSetCookieHeader(token);
        response.setHeader("Set-Cookie", headerValue);
    }

    private void clearTokenCookie(HttpServletResponse response) {
        // Max-Age=0 tells the browser to delete the cookie immediately.
        String headerValue = COOKIE_NAME + "=; Path=/; HttpOnly;" +
                (cookieSecure ? " Secure;" : "") +
                " SameSite=None; Max-Age=0";
        response.setHeader("Set-Cookie", headerValue);
    }

    private String buildSetCookieHeader(String token) {
        StringBuilder sb = new StringBuilder();
        sb.append(COOKIE_NAME).append("=").append(token);
        sb.append("; Path=/");
        sb.append("; HttpOnly");
        if (cookieSecure) sb.append("; Secure");
        sb.append("; SameSite=None");
        sb.append("; Max-Age=").append(cookieMaxAge);
        return sb.toString();
    }
}