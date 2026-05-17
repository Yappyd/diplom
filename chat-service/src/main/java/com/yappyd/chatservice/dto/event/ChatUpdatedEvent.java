package com.yappyd.chatservice.dto.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChatUpdatedEvent(
        UUID chatId,
        String title,
        OffsetDateTime updatedAt
) {
}