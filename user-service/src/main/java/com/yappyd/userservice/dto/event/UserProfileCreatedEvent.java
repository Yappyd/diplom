package com.yappyd.userservice.dto.event;

import java.util.UUID;

public record UserProfileCreatedEvent(
        UUID userId
) {
}