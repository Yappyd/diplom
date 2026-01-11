package com.yappyd.authservice.dto;

import java.time.Instant;

public record RefreshResponse(
        String accessToken,
        String refreshToken,
        Instant expiresAt
) {
}