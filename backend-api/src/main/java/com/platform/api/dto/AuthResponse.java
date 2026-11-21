package com.platform.api.dto;

import java.util.UUID;

public record AuthResponse(
        UserDto user,
        String accessToken,
        String refreshToken,
        String tokenType
) {
    public record UserDto(
            UUID id,
            String email,
            String role
    ) {}

    public AuthResponse(String accessToken, String refreshToken) {
        this(null, accessToken, refreshToken, "Bearer");
    }

    public static AuthResponse of(UUID id, String email, String role, String accessToken, String refreshToken) {
        return new AuthResponse(
                new UserDto(id, email, role),
                accessToken,
                refreshToken,
                "Bearer"
        );
    }
}