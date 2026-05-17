package com.yappyd.chatservice.component;

import com.yappyd.chatservice.dto.event.ChatCreatedEvent;
import com.yappyd.chatservice.dto.event.ChatDeletedEvent;
import com.yappyd.chatservice.dto.event.ChatParticipantAddedEvent;
import com.yappyd.chatservice.dto.event.ChatParticipantRemovedEvent;
import com.yappyd.chatservice.dto.event.ChatParticipantUpdatedEvent;
import com.yappyd.chatservice.dto.event.ChatUpdatedEvent;
import com.yappyd.chatservice.enums.ChatType;
import com.yappyd.chatservice.enums.ParticipantRole;
import com.yappyd.chatservice.exception.InvalidChatParticipantException;
import com.yappyd.chatservice.model.Chat;
import com.yappyd.chatservice.model.ChatParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatUiEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishChatCreated(Chat chat, List<UUID> participantIds) {
        applicationEventPublisher.publishEvent(
                new ChatCreatedEvent(
                        chat.getId(),
                        chat.getType().name(),
                        chat.getTitle(),
                        chat.getCreatedBy(),
                        participantIds,
                        chat.getCreatedAt(),
                        chat.getUpdatedAt()
                )
        );
    }

    public void publishChatUpdated(Chat chat) {
        applicationEventPublisher.publishEvent(
                new ChatUpdatedEvent(
                        chat.getId(),
                        chat.getTitle(),
                        chat.getUpdatedAt()
                )
        );
    }

    public void publishChatDeleted(UUID chatId, List<UUID> participantIds) {
        applicationEventPublisher.publishEvent(
                new ChatDeletedEvent(
                        chatId,
                        participantIds
                )
        );
    }

    public void publishParticipantAdded(ChatParticipant participant) {
        applicationEventPublisher.publishEvent(
                new ChatParticipantAddedEvent(
                        participant.getId().getChatId(),
                        participant.getId().getUserId(),
                        participant.getNickname(),
                        participant.getRole().name(),
                        canDeleteAnyMessages(participant.getRole()),
                        participant.getJoinedAt(),
                        participant.getUpdatedAt()
                )
        );
    }

    public void publishParticipantUpdated(ChatParticipant participant) {
        applicationEventPublisher.publishEvent(
                new ChatParticipantUpdatedEvent(
                        participant.getId().getChatId(),
                        participant.getId().getUserId(),
                        participant.getNickname(),
                        participant.getRole().name(),
                        canDeleteAnyMessages(participant.getRole()),
                        participant.getUpdatedAt()
                )
        );
    }

    public void publishParticipantRemoved(UUID chatId, UUID userId, UUID removedByUserId) {
        applicationEventPublisher.publishEvent(
                new ChatParticipantRemovedEvent(
                        chatId,
                        userId,
                        removedByUserId
                )
        );
    }

    private boolean canDeleteAnyMessages(ParticipantRole role) {
        if (role == null) {
            throw new InvalidChatParticipantException("participant role must not be null");
        }

        return role == ParticipantRole.OWNER || role == ParticipantRole.ADMIN;
    }
}