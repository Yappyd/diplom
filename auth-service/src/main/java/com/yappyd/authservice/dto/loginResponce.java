package com.yappyd.authservice.dto;

import java.time.Instant;

public record loginResponce(
        String accessToken,
        String refreshToken,
        Instant expiresAt
) {
}
