package com.nexerp.auth.api;

public record AuthLoginResponse(
        String accessToken,
        String tokenType,
        String refreshToken,
        long expiresIn
) {
}
