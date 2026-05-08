package com.edutech.edutechbackend.auth.controller;

import com.edutech.edutechbackend.auth.dto.UserLoginRequest;
import com.edutech.edutechbackend.auth.dto.UserRegisterRequest;
import com.edutech.edutechbackend.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Auth via Bearer token in response body.
 *
 * Why not HttpOnly cookies:
 *   Vercel (frontend) and Render (backend) are different domains.
 *   Chrome 130+ blocks cross-site cookies without the "Partitioned" attribute,
 *   and SameSite=None cookies are increasingly restricted by browser policy.
 *   Returning the token in the response body and sending it as an
 *   "Authorization: Bearer <token>" header is the correct, future-proof
 *   approach for cross-origin deployments.
 *
 * Token storage on the frontend:
 *   The token is stored in React context (memory only) — NOT localStorage.
 *   It is lost on page refresh, which triggers a silent re-auth check via
 *   a refresh token flow (or prompts re-login — acceptable for this app).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequest request) {
        authService.register(request);
        return ResponseEntity
                .status(201)
                .body(Map.of("message", "Account created successfully."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(Map.of(
                "message", "Login successful.",
                "token", token
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Stateless JWT — logout is client-side only (frontend discards the token)
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }
}