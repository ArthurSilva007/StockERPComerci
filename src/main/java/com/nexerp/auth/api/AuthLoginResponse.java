package com.nexerp.auth.api;

public record AuthLoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
