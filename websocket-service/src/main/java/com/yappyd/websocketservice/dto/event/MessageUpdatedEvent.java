package com.yappyd.websocketservice.dto.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageUpdatedEvent(
        UUID messageId,
        UUID chatId,
        UUID senderId,
        String content,
        OffsetDateTime updatedAt
) {
}