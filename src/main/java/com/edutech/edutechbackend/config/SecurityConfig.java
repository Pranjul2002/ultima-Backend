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
 * SecurityConfig — HttpOnly cookie edition
 * ─────────────────────────────────────────────────────────────────────────────
 * Key changes from the Bearer-token version:
 *
 *  1. allowCredentials(true) is REQUIRED — without it the browser strips the
 *     Set-Cookie header and the auth cookie is never stored.
 *
 *  2. allowedOrigins must be an EXPLICIT list (never "*") when credentials
 *     are enabled. Wildcards + credentials are blocked by the CORS spec.
 *
 *  3. "Set-Cookie" is added to exposedHeaders so the browser can read the
 *     header in preflight responses (some older browsers need this).
 *
 *  4. CSRF: still disabled. Rationale:
 *       - SameSite=None means the cookie IS sent on cross-origin requests,
 *         so SameSite alone does NOT prevent CSRF here.
 *       - Our frontend is a separate origin (Vercel), so classic CSRF via
 *         a same-site form post is not a threat vector.
 *       - For full CSRF protection on a cross-origin cookie setup, use the
 *         "double-submit cookie" pattern or a custom header (e.g. X-Requested-With).
 *       - Simpler alternative: add a CSRF token in the login response body
 *         and require it as a header on state-changing requests.
 *
 *  5. All other security headers are unchanged (HSTS, CSP, X-Frame, etc.).
 */
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF disabled — see class-level Javadoc for reasoning.
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(cto -> {})
                        .referrerPolicy(referrer ->
                                referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )
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

                .authorizeHttpRequests(auth -> auth
                        // Health probes
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()

                        // Auth endpoints — public (login sets the cookie, logout clears it)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public catalog — read-only, no login required
                        .requestMatchers(HttpMethod.GET, "/api/catalog/**").permitAll()

                        // Public competitive exam test listing — read-only, no login required
                        .requestMatchers(HttpMethod.GET, "/api/prep/**").permitAll()

                        // Upskilling public endpoints
                        .requestMatchers("/api/sources/**").permitAll()
                        .requestMatchers("/api/chat/**").permitAll()
                        .requestMatchers("/api/assessment/**").permitAll()

                        // Everything else requires a valid session cookie
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Explicit origin list — "*" is forbidden when allowCredentials = true.
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                frontendUrl
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Requested-With"));

        // Expose Set-Cookie so the browser can process the auth cookie on login.
        config.setExposedHeaders(List.of("Set-Cookie", "X-Total-Count"));

        // REQUIRED for HttpOnly cookie auth — without this the browser strips
        // the Set-Cookie response header and the cookie is never stored.
        config.setAllowCredentials(true);

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}