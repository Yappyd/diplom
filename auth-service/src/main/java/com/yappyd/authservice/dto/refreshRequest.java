package com.yappyd.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record refreshRequest(
        @NotBlank(message = "REFRESH_TOKEN_EMPTY")
        String refreshToken
) {
}
