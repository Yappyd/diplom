package com.yappyd.authservice.dto;

import java.time.Instant;

public record refreshResponce(
        String accessToken,
        String refreshToken,
        Instant expiresAt
) {
}