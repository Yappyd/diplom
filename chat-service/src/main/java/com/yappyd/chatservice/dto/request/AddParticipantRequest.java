package com.yappyd.chatservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddParticipantRequest(
        @NotNull(message = "userId must not be null")
        UUID userId
) {
}