package com.yappyd.websocketservice.dto.event;

import java.util.UUID;

public record ChatMessagePermissionUpsertedEvent(
        UUID chatId,
        UUID userId,
        boolean canDeleteAnyMessages
) {
}