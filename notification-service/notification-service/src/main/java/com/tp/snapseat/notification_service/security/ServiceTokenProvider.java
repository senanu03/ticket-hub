package com.tp.snapseat.notification_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class ServiceTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public ServiceTokenProvider(
            @Value("${tickethub.security.jwt-secret:SnapSeatVersion1JwtSecretKeyForTesting123456}") String secret,
            @Value("${tickethub.security.service-token-expiration-ms:300000}") long expirationMs) {

        this.secretKey =
                Keys.hmacShaKeyFor(
                        secret.getBytes(StandardCharsets.UTF_8));

        this.expirationMs = expirationMs;
    }

    public String createServiceToken() {

        Instant now = Instant.now();

        return Jwts.builder()
                .subject("notification-service")
                .claim("roles", "SERVICE")
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plusMillis(expirationMs)))
                .signWith(secretKey)
                .compact();
    }
}