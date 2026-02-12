package com.yappyd.authservice.dto.rabbitmq;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String phoneNumber,
        OffsetDateTime createdAt
) {
}
