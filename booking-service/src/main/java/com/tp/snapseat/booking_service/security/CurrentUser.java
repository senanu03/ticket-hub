package com.tp.snapseat.booking_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Authenticated caller, derived only from the validated JWT - never from request input. */
public record CurrentUser(String id, String email, boolean admin) {

    public static CurrentUser from(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new IllegalStateException("No JWT authentication present");
        }
        Jwt jwt = jwtAuth.getToken();
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return new CurrentUser(jwt.getSubject(), jwt.getClaimAsString("email"), admin);
    }
}
