package com.yappyd.chatservice.component;

import com.yappyd.chatservice.dto.event.ChatMessagePermissionDeletedEvent;
import com.yappyd.chatservice.dto.event.ChatMessagePermissionUpsertedEvent;
import com.yappyd.chatservice.enums.ParticipantRole;
import com.yappyd.chatservice.exception.InvalidChatParticipantException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatMessagePermissionEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishPermissionUpserted(UUID chatId, UUID userId, boolean canDeleteAnyMessages) {
        applicationEventPublisher.publishEvent(new ChatMessagePermissionUpsertedEvent(chatId, userId, canDeleteAnyMessages));
    }

    public void publishPermissionUpserted(UUID chatId, UUID userId, ParticipantRole role) {
        publishPermissionUpserted(chatId, userId, canDeleteAnyMessages(role));
    }

    public void publishPermissionDeleted(UUID chatId, UUID userId) {
        applicationEventPublisher.publishEvent(new ChatMessagePermissionDeletedEvent(chatId, userId));
    }

    private boolean canDeleteAnyMessages(ParticipantRole role) {
        if (role == null) {
            throw new InvalidChatParticipantException("participant role must not be null");
        }
        return role == ParticipantRole.OWNER || role == ParticipantRole.ADMIN;
    }
}