package com.yappyd.authservice.dto;

import java.time.Instant;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Instant expiresAt
) {
}
