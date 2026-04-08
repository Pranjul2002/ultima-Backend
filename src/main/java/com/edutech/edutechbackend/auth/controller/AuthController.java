package com.edutech.edutechbackend.auth.controller;

import com.edutech.edutechbackend.auth.dto.UserLoginRequest;
import com.edutech.edutechbackend.auth.dto.UserRegisterRequest;
import com.edutech.edutechbackend.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
                .body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletResponse response
    ) {
        String token = authService.login(request);

        String cookie = "jwt=" + token +
                "; HttpOnly" +
                "; Path=/" +
                "; Max-Age=86400" +
                "; SameSite=Strict";

        response.setHeader("Set-Cookie", cookie);

        return ResponseEntity.ok(Map.of("message", "Login successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        String cookie = "jwt=;" +
                " HttpOnly;" +
                " Path=/;" +
                " Max-Age=0;" +
                " SameSite=Strict";

        response.setHeader("Set-Cookie", cookie);

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}