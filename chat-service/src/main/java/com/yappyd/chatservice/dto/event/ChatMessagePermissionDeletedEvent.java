package com.yappyd.chatservice.dto.event;

import java.util.UUID;

public record ChatMessagePermissionDeletedEvent(
        UUID chatId,
        UUID userId
) {
}