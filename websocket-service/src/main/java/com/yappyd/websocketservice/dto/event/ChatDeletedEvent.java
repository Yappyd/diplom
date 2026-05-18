package com.yappyd.websocketservice.dto.event;

import java.util.List;
import java.util.UUID;

public record ChatDeletedEvent(
        UUID chatId,
        List<UUID> participantIds
) {
}