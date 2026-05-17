package com.yappyd.chatservice.dto.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChatParticipantAddedEvent(
        UUID chatId,
        UUID userId,
        String nickname,
        String role,
        boolean canDeleteAnyMessages,
        OffsetDateTime joinedAt,
        OffsetDateTime updatedAt
) {
}