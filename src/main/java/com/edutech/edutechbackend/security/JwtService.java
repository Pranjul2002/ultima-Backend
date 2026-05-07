package com.edutech.edutechbackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Production-hardened JwtService
 * ─────────────────────────────────────────────────────────────────────────────
 * Changes from dev version:
 *  1. Validates key length at startup — throws early if JWT_SECRET is too short
 *  2. Distinct exception types: TokenExpiredException vs InvalidTokenException
 *  3. isTokenExpired() utility for token refresh flows
 *  4. Expiration comes from config (not hardcoded 86400000)
 *  5. Logs invalid tokens at WARN, not ERROR (to avoid log flooding)
 */
@Service
@Slf4j
public class JwtService {

    /** Minimum 256-bit (32-byte) key required by HMAC-SHA256 */
    private static final int MIN_KEY_BYTES = 32;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET is too short: " + keyBytes.length + " bytes. " +
                            "Minimum required: " + MIN_KEY_BYTES + " bytes (256 bits). " +
                            "Generate one with: openssl rand -hex 32"
            );
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.info("JwtService initialized. Token TTL: {} ms", jwtExpirationMs);
    }

    /**
     * Generates a signed JWT for the given email (subject).
     */
    public String generateToken(String email) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Extracts the email (subject) from a valid, non-expired token.
     *
     * @throws TokenExpiredException  if the token is well-formed but expired
     * @throws InvalidTokenException  if the token is malformed or signature invalid
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Returns true if the token's expiry is in the past.
     * Useful for token-refresh endpoint logic.
     */
    public boolean isTokenExpired(String token) {
        try {
            return parseClaims(token).getExpiration().before(new Date());
        } catch (TokenExpiredException e) {
            return true;
        } catch (InvalidTokenException e) {
            return false; // can't determine expiry on invalid token
        }
    }

    /**
     * Returns the expiration date of a token.
     */
    public Date getExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired for subject: {}", e.getClaims().getSubject());
            throw new TokenExpiredException("JWT token has expired");
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            throw new InvalidTokenException("JWT token is invalid");
        }
    }

    // ── Inner exception types (no dependency on Spring Web) ──────────────────

    public static class TokenExpiredException extends RuntimeException {
        public TokenExpiredException(String msg) { super(msg); }
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String msg) { super(msg); }
    }
}