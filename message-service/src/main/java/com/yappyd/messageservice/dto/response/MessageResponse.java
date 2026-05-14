package com.yappyd.messageservice.dto.response;

import com.yappyd.messageservice.model.Message;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID messageId,
        UUID chatId,
        UUID senderId,
        String content,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime deletedAt,
        boolean edited,
        boolean deleted
) {

    public static MessageResponse from(Message message) {
        boolean deleted = message.isDeleted();

        return new MessageResponse(
                message.getMessageId(),
                message.getChatId(),
                message.getSenderId(),
                deleted ? null : message.getContent(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getDeletedAt(),
                message.isEdited(),
                deleted
        );
    }
}