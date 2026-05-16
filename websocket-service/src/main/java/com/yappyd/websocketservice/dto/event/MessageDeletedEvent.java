package com.yappyd.websocketservice.dto.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageDeletedEvent(
        UUID messageId,
        UUID chatId,
        OffsetDateTime deletedAt
) {
}