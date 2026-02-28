package com.yappyd.userservice.dto.responce;

import java.time.Instant;

public record ErrorResponse(
        String code,
        String message,
        int status,
        String path,
        Instant Timestamp
) {
}

