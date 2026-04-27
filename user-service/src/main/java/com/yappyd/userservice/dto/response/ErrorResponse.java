package com.yappyd.userservice.dto.response;

import java.time.Instant;

public record ErrorResponse(
        String code,
        String message,
        int status,
        String path,
        Instant timestamp
) {
}

