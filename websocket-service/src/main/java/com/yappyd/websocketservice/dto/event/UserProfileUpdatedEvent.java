package com.yappyd.websocketservice.dto.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserProfileUpdatedEvent(
        UUID userId,
        String firstName,
        String lastName,
        String tag,
        OffsetDateTime updatedAt
) {
}