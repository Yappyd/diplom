package com.yappyd.chatservice.dto.event;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ChatCreatedEvent(
        UUID chatId,
        String type,
        String title,
        UUID createdBy,
        List<UUID> participantIds,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}