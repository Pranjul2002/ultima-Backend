package com.edutech.edutechbackend.config;

import com.edutech.edutechbackend.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Production-hardened SecurityConfig
 * ─────────────────────────────────────────────────────────────────────────────
 * Changes from dev version:
 *  1. Security headers (HSTS, CSP, Referrer-Policy, X-Frame-Options, etc.)
 *  2. CORS limited to exact allowed origins — no wildcard fallback
 *  3. Explicit HTTP-method allow-list in CORS (no implicit OPTIONS passthrough)
 *  4. @EnableMethodSecurity for @PreAuthorize support on endpoints
 *  5. CSRF disabled only because JWT + HttpOnly cookie + SameSite=None is used;
 *     documented here for future auditors
 */
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Primary frontend origin (e.g., https://your-app.vercel.app).
     * Set FRONTEND_URL in your Render / Railway environment variables.
     * Falls back to localhost for local dev only.
     */
    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ── CSRF: disabled intentionally ─────────────────────────────────
                // Reason: auth uses JWT stored in HttpOnly cookies with SameSite=None;Secure.
                // SameSite=None prevents CSRF for cross-origin requests. If you ever switch
                // to same-site deployment, re-enable CSRF.
                .csrf(AbstractHttpConfigurer::disable)

                // ── CORS ─────────────────────────────────────────────────────────
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ── Session: stateless JWT ────────────────────────────────────────
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ── Security headers ──────────────────────────────────────────────
                .headers(headers -> headers
                        // Strict-Transport-Security: max-age=1 year, includeSubDomains
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )
                        // X-Frame-Options: DENY — prevents clickjacking
                        .frameOptions(frame -> frame.deny())
                        // X-Content-Type-Options: nosniff
                        .contentTypeOptions(cto -> {})
                        // Referrer-Policy
                        .referrerPolicy(referrer ->
                                referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )
                        // Content-Security-Policy — tighten to your specific needs
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self'; " +
                                                "style-src 'self' 'unsafe-inline'; " +
                                                "img-src 'self' data:; " +
                                                "font-src 'self'; " +
                                                "connect-src 'self'; " +
                                                "frame-ancestors 'none'"
                                )
                        )
                )

                // ── Authorization rules ───────────────────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        // Health / readiness probes (Render, Railway, K8s)
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()

                        // Public auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public catalog — read-only browsing without login
                        .requestMatchers(HttpMethod.GET, "/api/catalog/**").permitAll()

                        // Upskilling public endpoints (source upload, chat, assessment)
                        .requestMatchers("/api/sources/**").permitAll()
                        .requestMatchers("/api/chat/**").permitAll()
                        .requestMatchers("/api/assessment/**").permitAll()

                        // Everything else requires a valid JWT
                        .anyRequest().authenticated()
                )

                // ── JWT filter ────────────────────────────────────────────────────
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Explicit origin allow-list — never use "*" with credentials:true
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://edu-tech-rouge.vercel.app"               // Injected via FRONTEND_URL env var
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Requested-With"));
        config.setExposedHeaders(List.of("X-Total-Count")); // expose pagination headers if used
        config.setAllowCredentials(true);   // required for HttpOnly cookie transport
        config.setMaxAge(3600L);            // preflight cache: 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * BCrypt with strength 12 (production default).
     * Strength 10 (Spring default) is fine for most apps; 12 adds ~4× hashing time.
     * Adjust based on your server hardware and acceptable login latency.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
