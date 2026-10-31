package com.tp.snapseat.auth_service.service;

import com.tp.snapseat.auth_service.dto.LoginRequest;
import com.tp.snapseat.auth_service.dto.LoginResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class AuthService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public AuthService(
            @Value("${tickethub.security.jwt-secret}") String secret,
            @Value("${tickethub.security.jwt-expiration-ms}") long expirationMs) {

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
        this.expirationMs = expirationMs;
    }

    public LoginResponse login(LoginRequest request) {

        if ("admin".equals(request.username())
                && "admin123".equals(request.password())) {

            return generateToken(request.username(), "ADMIN");
        }

        if ("customer".equals(request.username())
                && "customer123".equals(request.password())) {

            return generateToken(request.username(), "CUSTOMER");
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password"
        );
    }

    private LoginResponse generateToken(String username, String role) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        String token = Jwts.builder()
                .subject(username)
                .claim("roles", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();

        return new LoginResponse(
                token,
                "Bearer",
                expirationMs / 1000,
                username,
                role
        );
    }
}
