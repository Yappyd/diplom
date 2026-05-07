package com.yappyd.chatservice.dto.response;

import com.yappyd.chatservice.enums.ParticipantRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChatParticipantResponse(
        UUID userId,
        ParticipantRole role,
        String nickname,
        OffsetDateTime joinedAt,
        OffsetDateTime updatedAt
) {
}
