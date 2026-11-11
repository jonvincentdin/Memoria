package com.csit321g1.lukydizy.memoria.controller;

import java.util.Map;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Endpoint to fetch the CSRF token for frontend clients and API testing.
     * Accessible without login.
     */
    @GetMapping("/csrf")
    public Map<String, String> getCsrfToken(CsrfToken token) {
        if (token == null) {
            return Map.of("token", "", "headerName", "X-XSRF-TOKEN");
        }
        return Map.of(
            "token", token.getToken(),
            "headerName", token.getHeaderName(),
            "parameterName", token.getParameterName()
        );
    }
}
