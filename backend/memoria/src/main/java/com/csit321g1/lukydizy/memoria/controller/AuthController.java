package com.csit321g1.lukydizy.memoria.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.csit321g1.lukydizy.memoria.dto.UserDto.LoginRequest;
import com.csit321g1.lukydizy.memoria.dto.UserDto.RegisterRequest;
import com.csit321g1.lukydizy.memoria.dto.UserDto.UserProfile;
import com.csit321g1.lukydizy.memoria.entity.UserEntity;
import com.csit321g1.lukydizy.memoria.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final SecurityContextRepository securityContextRepository;

    /** Creates the controller with its authentication dependencies. */
    public AuthController(
        UserService userService,
        SessionAuthenticationStrategy sessionAuthenticationStrategy,
        SecurityContextRepository securityContextRepository
    ) {
        this.userService = userService;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.securityContextRepository = securityContextRepository;
    }

    /** Returns the CSRF token required for state-changing requests. */
    @GetMapping("/csrf")
    public Map<String, String> getCsrfToken(CsrfToken token) {
        return Map.of(
            "token", token.getToken(),
            "headerName", token.getHeaderName(),
            "parameterName", token.getParameterName()
        );
    }

    /** Registers a user and starts an authenticated session. */
    @PostMapping("/register")
    public UserProfile register(
        @Valid @RequestBody RegisterRequest requestBody,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        // Validates, hashes the password and saves the new user.
        UserProfile profile = userService.register(requestBody);
        // Log the new user in right away by creating a session.
        establishSession(profile.userId(), request, response);
        // 201 Created.
        response.setStatus(HttpStatus.CREATED.value());
        return profile;
    }

    /** Verifies credentials and starts an authenticated session. */
    @PostMapping("/login")
    public UserProfile login(
        @Valid @RequestBody LoginRequest requestBody,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        // Throws 401 if the email or password is wrong.
        UserEntity user = userService.authenticate(requestBody);
        // Create the session; the browser receives a JSESSIONID cookie.
        establishSession(user.getId(), request, response);
        return new UserProfile(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }

    /** Returns the profile belonging to the current authenticated session. */
    @GetMapping("/me")
    public UserProfile currentUser(Authentication authentication) {
        // The session stores the user ID as the authentication name.
        return userService.getProfile(authentication.getName());
    }

    /** Stores the authenticated user in the HTTP session. */
    private void establishSession(
        String userId,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        // Build a logged-in identity: the user ID with the ROLE_USER role.
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
            userId,
            null,
            java.util.List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        // Rotate the session ID and CSRF token (prevents session fixation).
        sessionAuthenticationStrategy.onAuthentication(authentication, request, response);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        // Save the login into the HTTP session so later requests stay authenticated.
        securityContextRepository.saveContext(context, request, response);
    }
}
