package com.edutech.edutechbackend.security;

import com.edutech.edutechbackend.user.entity.User;
import com.edutech.edutechbackend.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Reads the JWT from the HttpOnly cookie named "auth_token".
 *
 * Flow:
 *   1. Extract cookie value — if absent, continue filter chain unauthenticated.
 *   2. Parse and validate the JWT via JwtService.
 *   3. Load the User from the database.
 *   4. Set a UsernamePasswordAuthenticationToken in the SecurityContext.
 *
 * Security notes:
 *   - HttpOnly cookies cannot be read by JavaScript → XSS-proof token storage.
 *   - The cookie is SameSite=None;Secure — required for cross-origin deployments.
 *   - On any JWT error, return 401 JSON (never a redirect) so the frontend
 *     can handle it cleanly.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    /** Must match AuthController.COOKIE_NAME */
    private static final String COOKIE_NAME = "auth_token";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractCookieToken(request);

        // No cookie present — continue as anonymous; SecurityConfig decides
        // whether the endpoint requires authentication.
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Already authenticated earlier in the chain (shouldn't happen with
        // stateless sessions, but guard anyway).
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String email = jwtService.extractEmail(token);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.warn("Cookie JWT auth failed on {}: {}", request.getRequestURI(), e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"error\":\"invalid_token\",\"message\":\"Invalid or expired session. Please log in again.\"}"
            );
        }
    }

    /**
     * Extracts the JWT string from the request's cookies.
     * Returns null if the cookie is absent or blank.
     */
    private String extractCookieToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> v != null && !v.isBlank())
                .findFirst()
                .orElse(null);
    }
}