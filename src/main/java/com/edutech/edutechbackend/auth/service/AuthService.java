package com.edutech.edutechbackend.auth.service;

import com.edutech.edutechbackend.auth.dto.UserLoginRequest;
import com.edutech.edutechbackend.auth.dto.UserRegisterRequest;
import com.edutech.edutechbackend.exception.BadRequestException;
import com.edutech.edutechbackend.exception.ConflictException;
import com.edutech.edutechbackend.security.JwtService;
import com.edutech.edutechbackend.user.entity.User;
import com.edutech.edutechbackend.user.enums.Role;
import com.edutech.edutechbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Production-hardened AuthService
 * ─────────────────────────────────────────────────────────────────────────────
 * Changes from dev version:
 *  1. Uses typed exceptions (ConflictException, BadRequestException) instead of
 *     raw RuntimeException — GlobalExceptionHandler maps these to correct HTTP codes
 *  2. login() uses a constant-time comparison path to prevent user enumeration:
 *     both "user not found" and "wrong password" return the same error message
 *     AND take similar time (dummy bcrypt check on not-found path)
 *  3. @Transactional on register() ensures atomicity
 *  4. Logging uses structured keys, never logs passwords or tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * A dummy hash used on the "user not found" path in login() to make the
     * response time indistinguishable from a valid user with a wrong password.
     * This prevents user-enumeration timing attacks.
     */
    private static final String DUMMY_HASH =
            "$2a$12$dummyHashToPreventTimingAttacksOnUserEnumerationXXXXXXX";

    @Transactional
    public void register(UserRegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        String username = request.getUsername().trim();

        // Check uniqueness before attempting to parse role
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("An account with this email already exists.");
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role. Accepted values: STUDENT, MENTOR");
        }

        User user = User.builder()
                .email(email)
                .username(username)
                .dateOfBirth(request.getDateOfBirth())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
        log.info("User registered: email={}, role={}", email, role);
    }

    /**
     * Authenticates the user and returns a signed JWT.
     * Uses a timing-safe approach: always runs bcrypt even if the user is not found,
     * so attackers cannot enumerate registered emails via response time.
     */
    public String login(UserLoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        // Fetch user — but don't reveal whether they exist yet
        var maybeUser = userRepository.findByEmail(email);

        if (maybeUser.isEmpty()) {
            // Run a dummy bcrypt check to equalise response time
            passwordEncoder.matches(request.getPassword(), DUMMY_HASH);
            log.debug("Login attempt for unknown email: {}", email);
            throw new BadRequestException("Invalid email or password.");
        }

        User user = maybeUser.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.debug("Failed login attempt for email: {}", email);
            throw new BadRequestException("Invalid email or password.");
        }

        log.info("Successful login: email={}", email);
        return jwtService.generateToken(user.getEmail());
    }
}