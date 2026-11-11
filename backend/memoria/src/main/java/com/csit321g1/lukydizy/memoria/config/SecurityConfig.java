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

    /** BCrypt encoder bean — inject wherever passwords are hashed or verified. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(CsrfTokenRepository csrfTokenRepository) {
        return new CompositeSessionAuthenticationStrategy(List.of(
            new ChangeSessionIdAuthenticationStrategy(),
            new CsrfAuthenticationStrategy(csrfTokenRepository)
        ));
    }

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        CsrfTokenRepository csrfTokenRepository,
        SecurityContextRepository securityContextRepository
    ) throws Exception {
        http
            .cors(cors -> {
                cors.configurationSource(corsConfigurationSource());
            })
            .csrf(csrf -> {
                csrf.csrfTokenRepository(csrfTokenRepository);
            })
            .securityContext(context -> {
                context
                    .securityContextRepository(securityContextRepository)
                    .requireExplicitSave(true);
            })
            .authorizeHttpRequests(auth -> {
                auth
                    .requestMatchers(
                        "/actuator/health",
                        "/api/auth/csrf",
                        "/api/auth/register",
                        "/api/auth/login"
                    ).permitAll()
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll();
            })
            .logout(logout -> {
                logout
                    .logoutUrl("/api/auth/logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessHandler(jsonLogoutSuccessHandler());
            })
            // Return JSON 401 instead of a redirect to a login page
            .exceptionHandling(ex -> {
                ex
                    .authenticationEntryPoint((req, res, e) -> {
                        res.setStatus(HttpStatus.UNAUTHORIZED.value());
                        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        res.getWriter().write("{\"error\":\"Unauthorized\"}");
                    })
                    .accessDeniedHandler((req, res, e) -> {
                        res.setStatus(HttpStatus.FORBIDDEN.value());
                        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        res.getWriter().write("{\"error\":\"Forbidden\"}");
                    });
            })
            // No form login or HTTP Basic — session-based, controlled by /api/auth
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    private LogoutSuccessHandler jsonLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Logged out\"}");
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Allow the Vite dev server to send cookies
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
