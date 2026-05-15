package com.yappyd.chatservice.dto.event;

import java.util.UUID;

public record ChatMessagePermissionUpsertedEvent(
        UUID chatId,
        UUID userId,
        boolean canDeleteAnyMessages
) {
}