package com.csit321g1.lukydizy.memoria.service;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Reads the logged-in user's ID from the authenticated session. */
public final class CurrentUser {

    private CurrentUser() {
    }

    /** Returns the ID of the user stored in the current session. */
    public static String id() {
        // Spring Security keeps the logged-in user of the current request here.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
            || !authentication.isAuthenticated()
            || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("No authenticated user in the current session.");
        }
        // AuthController stores the user ID as the name when the session is created.
        return authentication.getName();
    }
}
