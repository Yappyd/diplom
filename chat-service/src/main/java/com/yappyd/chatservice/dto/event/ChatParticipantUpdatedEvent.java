package com.yappyd.chatservice.dto.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChatParticipantUpdatedEvent(
        UUID chatId,
        UUID userId,
        String nickname,
        String role,
        boolean canDeleteAnyMessages,
        OffsetDateTime updatedAt
) {
}