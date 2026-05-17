package com.yappyd.messageservice.dto.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageDeletedEvent(
        UUID messageId,
        UUID chatId,
        OffsetDateTime deletedAt
) {
}