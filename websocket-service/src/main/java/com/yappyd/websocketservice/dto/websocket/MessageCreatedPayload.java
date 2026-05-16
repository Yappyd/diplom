package com.yappyd.websocketservice.dto.websocket;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageCreatedPayload(
        UUID messageId,
        UUID chatId,
        UUID senderId,
        String content,
        OffsetDateTime createdAt
) {
}