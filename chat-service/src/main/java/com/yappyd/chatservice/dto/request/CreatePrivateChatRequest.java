package com.yappyd.chatservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePrivateChatRequest(
        @NotNull(message = "targetUserId must not be null")
        UUID targetUserId
) {
}
