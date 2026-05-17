package com.yappyd.messageservice.dto.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageCreatedEvent(
        UUID messageId,
        UUID chatId,
        UUID senderId,
        String content,
        OffsetDateTime createdAt
) {
}