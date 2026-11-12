package com.csit321g1.lukydizy.memoria.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Creates the encoder used to hash and verify passwords. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Creates a CSRF token repository backed by the HTTP session. */
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    /** Creates a security context repository backed by the HTTP session. */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /** Rotates the session ID and CSRF token after authentication. */
    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(
        CsrfTokenRepository csrfTokenRepository
    ) {
        return new CompositeSessionAuthenticationStrategy(List.of(
            new ChangeSessionIdAuthenticationStrategy(),
            new CsrfAuthenticationStrategy(csrfTokenRepository)
        ));
    }

    /** Configures session authentication and access rules for the API. */
    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        CsrfTokenRepository csrfTokenRepository,
        SecurityContextRepository securityContextRepository
    ) throws Exception {
        http
            // Allow the React dev server to call the API with cookies.
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // POST/PUT/DELETE must send the CSRF token from GET /api/auth/csrf.
            .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))
            // Keep the logged-in user in the HTTP session.
            .securityContext(context -> context
                .securityContextRepository(securityContextRepository)
                .requireExplicitSave(true))
            // Which URLs are public and which need a login.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/health",
                    "/api/auth/csrf",
                    "/api/auth/register",
                    "/api/auth/login"
                ).permitAll()
                // Every other /api route (users, reviewers, attempts, ...) needs a session.
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll())
            // POST /api/auth/logout ends the session and deletes the cookie.
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler(jsonLogoutSuccessHandler()))
            // Return JSON 401/403 instead of redirecting to a login page.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"error\":\"Unauthorized\"}");
                })
                .accessDeniedHandler((request, response, exception) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"error\":\"Forbidden\"}");
                }))
            // No built-in login page or HTTP Basic; login goes through AuthController.
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /** Returns a JSON response after a successful logout. */
    private LogoutSuccessHandler jsonLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Logged out\"}");
        };
    }

    /** Configures credentialed API requests from the local Vite server. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Only the Vite dev server may call the API from a browser.
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // Lets the browser send the session cookie cross-origin.
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
