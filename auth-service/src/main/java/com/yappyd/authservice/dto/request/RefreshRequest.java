package com.yappyd.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "REFRESH_TOKEN_EMPTY")
        String refreshToken
) {
}
