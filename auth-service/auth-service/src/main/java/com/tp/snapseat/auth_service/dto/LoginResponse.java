package com.tp.snapseat.auth_service.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String username,
        String role
) {
}
