package com.yappyd.messageservice.dto.event;

import java.util.UUID;

public record ChatMessagePermissionUpsertedEvent(
        UUID chatId,
        UUID userId,
        boolean canDeleteAnyMessages
) {
}