package com.yappyd.authservice.dto.response;

import java.time.Instant;

public record RefreshResponse(
        String accessToken,
        String refreshToken,
        Instant expiresAt
) {
}