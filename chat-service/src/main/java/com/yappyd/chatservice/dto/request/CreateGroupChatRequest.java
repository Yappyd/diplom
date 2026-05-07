package com.yappyd.chatservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateGroupChatRequest(

        @NotBlank(message = "title must not be blank")
        @Size(max = 255, message = "title must not exceed 255 characters")
        String title,

        @NotNull(message = "participantIds must not be null")
        List<@NotNull(message = "participantId must not be null") UUID> participantIds

) {
}