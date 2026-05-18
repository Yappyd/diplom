package com.yappyd.websocketservice.dto.event;

import java.util.UUID;

public record ChatParticipantRemovedEvent(
        UUID chatId,
        UUID userId,
        UUID removedByUserId
) {
}