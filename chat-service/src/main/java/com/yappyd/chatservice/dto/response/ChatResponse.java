package com.yappyd.chatservice.dto.response;

import com.yappyd.chatservice.enums.ChatType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ChatResponse(
        UUID chatId,
        ChatType type,
        String title,
        UUID createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<UUID> participantIds
) {
}
