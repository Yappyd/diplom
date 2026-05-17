package com.yappyd.chatservice.service;

import com.yappyd.chatservice.component.ChatMessagePermissionEventPublisher;
import com.yappyd.chatservice.component.ChatUiEventPublisher;
import com.yappyd.chatservice.dto.request.UpdateChatRequest;
import com.yappyd.chatservice.dto.response.ChatResponse;
import com.yappyd.chatservice.enums.ChatType;
import com.yappyd.chatservice.enums.ParticipantRole;
import com.yappyd.chatservice.exception.ChatAccessDeniedException;
import com.yappyd.chatservice.exception.ChatNotFoundException;
import com.yappyd.chatservice.exception.InvalidChatException;
import com.yappyd.chatservice.mapper.ChatMapper;
import com.yappyd.chatservice.model.Chat;
import com.yappyd.chatservice.model.ChatParticipant;
import com.yappyd.chatservice.repository.ChatParticipantRepository;
import com.yappyd.chatservice.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final TransactionTemplate transactionTemplate;
    private final ChatMessagePermissionEventPublisher messagePermissionEventPublisher;
    private final ChatMapper chatMapper;
    private final ChatUiEventPublisher chatUiEventPublisher;
    private final KnownUserService knownUserService;

    public ChatResponse createGroupChat(UUID currentUserId, String title, List<UUID> participantIds) {
        validateGroupChat(currentUserId, title, participantIds);
        knownUserService.validateUserKnown(currentUserId);
        knownUserService.validateUsersKnown(participantIds);

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

            List<UUID> createdParticipantIds = participants.stream().map(participant -> participant.getId().getUserId()).toList();
            chatUiEventPublisher.publishChatCreated(chat, createdParticipantIds);

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

    @Transactional
    public ChatResponse updateChat(UUID currentUserId, UUID chatId, UpdateChatRequest request) {
        validateUpdateChat(currentUserId, chatId, request);

        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException(chatId));

        if (chat.getType() != ChatType.GROUP) {
            throw new InvalidChatException("Only group chats can be updated");
        }

        ChatParticipant currentParticipant = chatParticipantRepository
                .findByIdChatIdAndIdUserId(chatId, currentUserId)
                .orElseThrow(() -> new ChatAccessDeniedException(chatId));

        validateUpdateGroupChatPermission(chatId, currentParticipant);

        if (request.getTitle().isPresent()) {
            String newTitle = request.getTitle().get();
            chat.updateTitle(newTitle);
        }

        Chat savedChat = chatRepository.save(chat);

        chatUiEventPublisher.publishChatUpdated(savedChat);

        List<ChatParticipant> participants = chatParticipantRepository.findByIdChatId(chatId);
        List<UUID> participantIds = participants.stream().map(participant -> participant.getId().getUserId()).toList();

        return chatMapper.toChatResponse(savedChat, participantIds);
    }

    private void validateUpdateChat(UUID currentUserId, UUID chatId, UpdateChatRequest request) {
        if (currentUserId == null) {
            throw new InvalidChatException("currentUserId must not be null");
        }

        if (chatId == null) {
            throw new InvalidChatException("chatId must not be null");
        }

        if (request == null) {
            throw new InvalidChatException("request must not be null");
        }

        if (!request.getTitle().isPresent()) {
            throw new InvalidChatException("At least one field must be provided");
        }
    }

    private void validateUpdateGroupChatPermission(UUID chatId, ChatParticipant currentParticipant) {
        ParticipantRole role = currentParticipant.getRole();
        if (role == ParticipantRole.OWNER || role == ParticipantRole.ADMIN) return;
        throw new ChatAccessDeniedException(chatId);
    }

    @Transactional
    public void deleteGroupChat(UUID currentUserId, UUID chatId) {
        validateDeleteGroupChat(currentUserId, chatId);

        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException(chatId));

        if (chat.getType() != ChatType.GROUP) {
            throw new InvalidChatException("Only group chats can be deleted");
        }

        List<ChatParticipant> participants = chatParticipantRepository.findByIdChatId(chatId);

        if (participants.isEmpty()) {
            throw new IllegalStateException("Group participants not found for chatId: " + chatId);
        }

        ChatParticipant currentParticipant = participants.stream()
                .filter(participant -> participant.getId().getUserId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new ChatAccessDeniedException(chatId));

        if (currentParticipant.getRole() != ParticipantRole.OWNER) {
            throw new ChatAccessDeniedException(chatId);
        }

        List<UUID> participantIds = participants.stream().map(participant -> participant.getId().getUserId()).toList();

        chatRepository.delete(chat);

        participantIds.forEach(userId -> messagePermissionEventPublisher.publishPermissionDeleted(chatId, userId));
        chatUiEventPublisher.publishChatDeleted(chatId, participantIds);
    }

    private void validateDeleteGroupChat(UUID currentUserId, UUID chatId) {
        if (currentUserId == null) {
            throw new InvalidChatException("currentUserId must not be null");
        }

        if (chatId == null) {
            throw new InvalidChatException("chatId must not be null");
        }
    }
}