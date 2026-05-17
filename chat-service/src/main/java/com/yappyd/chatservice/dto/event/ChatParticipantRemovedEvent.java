package com.yappyd.chatservice.dto.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChatParticipantRemovedEvent(
        UUID chatId,
        UUID userId,
        UUID removedByUserId
) {
}