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
import java.util.Optional;

/**
 * Production-hardened JwtAuthFilter
 * ─────────────────────────────────────────────────────────────────────────────
 * Changes from dev version:
 *  1. Returns structured JSON 401 on expired token (not a silent passthrough
 *     that sends the request to protected endpoints unauthenticated)
 *  2. Returns structured JSON 401 on invalid/tampered token
 *  3. Uses Optional to avoid null checks on cookies
 *  4. User lookup failure handled gracefully (sends 401, not 500)
 *  5. No DB call if SecurityContext already has authentication (re-entrant safe)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request).orElse(null);

        if (token == null) {
            // No JWT present — pass through; Spring Security will reject if endpoint requires auth
            filterChain.doFilter(request, response);
            return;
        }

        // Already authenticated (e.g., filter re-entry)
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String email = jwtService.extractEmail(token);

            User user = userRepository.findByEmail(email).orElseThrow(() ->
                    new JwtService.InvalidTokenException("User not found for JWT subject")
            );

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);

        } catch (JwtService.TokenExpiredException e) {
            log.debug("Expired JWT on {}", request.getRequestURI());
            sendUnauthorized(response, "token_expired", "Your session has expired. Please log in again.");

        } catch (JwtService.InvalidTokenException e) {
            log.warn("Invalid JWT on {}: {}", request.getRequestURI(), e.getMessage());
            sendUnauthorized(response, "invalid_token", "Invalid authentication token.");

        } catch (Exception e) {
            log.error("Unexpected JWT filter error on {}: {}", request.getRequestURI(), e.getMessage());
            sendUnauthorized(response, "auth_error", "Authentication failed.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Optional<String> extractToken(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> "jwt".equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> v != null && !v.isBlank())
                .findFirst();
    }

    /**
     * Writes a JSON 401 response and terminates the filter chain.
     * Using structured JSON ensures the frontend can handle specific error codes.
     */
    private void sendUnauthorized(HttpServletResponse response, String code, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"error\":\"" + code + "\",\"message\":\"" + message + "\"}"
        );
    }
}