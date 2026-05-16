package com.yappyd.websocketservice.dto.event;

import java.util.UUID;

public record ChatMessagePermissionDeletedEvent(
        UUID chatId,
        UUID userId
) {
}