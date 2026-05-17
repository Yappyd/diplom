package com.yappyd.chatservice.service;

import com.yappyd.chatservice.component.ChatMessagePermissionEventPublisher;
import com.yappyd.chatservice.dto.response.ChatResponse;
import com.yappyd.chatservice.enums.ParticipantRole;
import com.yappyd.chatservice.exception.InvalidChatException;
import com.yappyd.chatservice.mapper.ChatMapper;
import com.yappyd.chatservice.model.Chat;
import com.yappyd.chatservice.model.ChatParticipant;
import com.yappyd.chatservice.repository.ChatParticipantRepository;
import com.yappyd.chatservice.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final TransactionTemplate transactionTemplate;
    private final ChatMessagePermissionEventPublisher messagePermissionEventPublisher;
    private final ChatMapper chatMapper;

    public ChatResponse createGroupChat(UUID currentUserId, String title, List<UUID> participantIds) {
        validateGroupChat(currentUserId, title, participantIds);

        Set<UUID> normalizedParticipantIds = normalizeGroupParticipants(currentUserId, participantIds);

        UUID createdChatId = transactionTemplate.execute(status -> {
            Chat chat = Chat.createGroupChat(currentUserId, title);
            chatRepository.save(chat);

            List<ChatParticipant> participants = normalizedParticipantIds.stream()
                    .map(userId -> {
                        ParticipantRole role = userId.equals(currentUserId)
                                ? ParticipantRole.OWNER
                                : ParticipantRole.MEMBER;

                        return new ChatParticipant(chat.getId(), userId, role);
                    })
                    .toList();

            chatParticipantRepository.saveAll(participants);

            participants.forEach(participant ->
                    messagePermissionEventPublisher.publishPermissionUpserted(
                            participant.getId().getChatId(),
                            participant.getId().getUserId(),
                            participant.getRole()
                    )
            );

            return chat.getId();
        });

        UUID chatId = Objects.requireNonNull(createdChatId, "createdChatId must not be null");
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new IllegalStateException("Created group chat not found: " + chatId));

        return chatMapper.toChatResponse(chat, normalizedParticipantIds.stream().toList());
    }

    private void validateGroupChat(UUID currentUserId, String title, List<UUID> participantIds) {
        if (currentUserId == null) {
            throw new InvalidChatException("currentUserId must not be null");
        }

        if (title == null || title.isBlank()) {
            throw new InvalidChatException("title must not be null or blank");
        }

        if (participantIds == null) {
            throw new InvalidChatException("participantIds must not be null");
        }

        if (participantIds.stream().anyMatch(Objects::isNull)) {
            throw new InvalidChatException("participantIds must not contain null values");
        }
    }

    private Set<UUID> normalizeGroupParticipants(UUID currentUserId, List<UUID> participantIds) {
        Set<UUID> normalizedParticipantIds = new LinkedHashSet<>();

        normalizedParticipantIds.add(currentUserId);
        normalizedParticipantIds.addAll(participantIds);

        return normalizedParticipantIds;
    }
}