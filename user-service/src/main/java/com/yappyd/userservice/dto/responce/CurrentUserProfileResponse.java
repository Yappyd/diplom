package com.yappyd.userservice.dto.responce;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CurrentUserProfileResponse(
        UUID userId,
        String phoneNumber,
        boolean phoneIsVisible,
        boolean profileCompleted,

        String firstName,
        String lastName,
        String tag,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
