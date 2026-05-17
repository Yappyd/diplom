package com.yappyd.chatservice.service;

import com.yappyd.chatservice.component.ChatMessagePermissionEventPublisher;
import com.yappyd.chatservice.dto.request.UpdateChatParticipantRequest;
import com.yappyd.chatservice.dto.response.ChatParticipantResponse;
import com.yappyd.chatservice.dto.response.ChatParticipantsResponse;
import com.yappyd.chatservice.enums.ChatType;
import com.yappyd.chatservice.enums.ParticipantRole;
import com.yappyd.chatservice.exception.*;
import com.yappyd.chatservice.mapper.ChatMapper;
import com.yappyd.chatservice.model.Chat;
import com.yappyd.chatservice.model.ChatParticipant;
import com.yappyd.chatservice.repository.ChatParticipantRepository;
import com.yappyd.chatservice.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatParticipantService {

    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessagePermissionEventPublisher messagePermissionEventPublisher;
    private final ChatMapper chatMapper;

    public ChatParticipantsResponse getChatParticipants(UUID currentUserId, UUID chatId) {
        if (currentUserId == null) {
            throw new InvalidChatException("currentUserId must not be null");
        }

        if (chatId == null) {
            throw new InvalidChatException("chatId must not be null");
        }

        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException(chatId));

        if (chat.getType() != ChatType.GROUP) {
            throw new InvalidChatException("Participants endpoint is available only for group chats");
        }

        List<ChatParticipant> participants = chatParticipantRepository.findByIdChatId(chatId);

        if (participants.isEmpty()) {
            throw new IllegalStateException("Group participants not found for chatId: " + chatId);
        }

        boolean currentUserIsParticipant = participants.stream()
                .anyMatch(participant -> participant.getId().getUserId().equals(currentUserId));

        if (!currentUserIsParticipant) {
            throw new ChatAccessDeniedException(chatId);
        }

        List<ChatParticipantResponse> participantResponses = participants.stream().map(chatMapper::toChatParticipantResponse).toList();

        return new ChatParticipantsResponse(participantResponses);
    }

    @Transactional
    public ChatParticipantsResponse addParticipant(UUID currentUserId, UUID chatId, UUID userId) {
        validateParticipantAction(currentUserId, chatId, userId);

        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException(chatId));

        if (chat.getType() != ChatType.GROUP) {
            throw new InvalidChatException("Participants can be added only to group chats");
        }

        List<ChatParticipant> participants = chatParticipantRepository.findByIdChatId(chatId);

        if (participants.isEmpty()) {
            throw new IllegalStateException("Group participants not found for chatId: " + chatId);
        }

        ChatParticipant currentParticipant = participants.stream()
                .filter(participant -> participant.getId().getUserId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new ChatAccessDeniedException(chatId));

        if (currentParticipant.getRole() != ParticipantRole.OWNER && currentParticipant.getRole() != ParticipantRole.ADMIN) {
            throw new ChatAccessDeniedException(chatId);
        }

        boolean userAlreadyParticipant = participants.stream()
                .anyMatch(participant -> participant.getId().getUserId().equals(userId));

        if (userAlreadyParticipant) {
            throw new ParticipantAlreadyExistsException(chatId, userId);
        }

        ChatParticipant newParticipant = new ChatParticipant(chatId, userId, ParticipantRole.MEMBER);
        chatParticipantRepository.save(newParticipant);
        messagePermissionEventPublisher.publishPermissionUpserted(chatId, userId, newParticipant.getRole());

        List<ChatParticipant> updatedParticipants = chatParticipantRepository.findByIdChatId(chatId);
        List<ChatParticipantResponse> participantResponses = updatedParticipants.stream().map(chatMapper::toChatParticipantResponse).toList();

        return new ChatParticipantsResponse(participantResponses);
    }

    @Transactional
    public Optional<ChatParticipantsResponse> removeParticipant(UUID currentUserId, UUID chatId, UUID userId, UUID newOwnerId) {
        validateParticipantAction(currentUserId, chatId, userId);

        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException(chatId));

        if (chat.getType() != ChatType.GROUP) {
            throw new InvalidChatException("Participants can be removed only from group chats");
        }

        boolean removingSelf = currentUserId.equals(userId);

        List<ChatParticipant> participants = chatParticipantRepository.findByIdChatId(chatId);

        if (participants.isEmpty()) {
            throw new IllegalStateException("Group participants not found for chatId: " + chatId);
        }

        ChatParticipant currentParticipant = participants.stream()
                .filter(participant -> participant.getId().getUserId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new ChatAccessDeniedException(chatId));

        ChatParticipant targetParticipant = participants.stream()
                .filter(participant -> participant.getId().getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ParticipantNotFoundException(chatId, userId));

        validateParticipantPermissionHierarchy(chatId, currentParticipant, targetParticipant);

        boolean ownerRemovesSelf = removingSelf && targetParticipant.getRole() == ParticipantRole.OWNER;

        boolean removingLastParticipant = participants.size() == 1;

        if (ownerRemovesSelf && removingLastParticipant) {
            chatRepository.delete(chat);
            messagePermissionEventPublisher.publishPermissionDeleted(chatId, userId);
            return Optional.empty();
        }

        if (ownerRemovesSelf) {
            ChatParticipant newOwner = transferOwnershipBeforeOwnerLeaves(chatId, userId, newOwnerId, participants);
            messagePermissionEventPublisher.publishPermissionUpserted(chatId, newOwner.getId().getUserId(), newOwner.getRole());
        }

        chatParticipantRepository.delete(targetParticipant);
        messagePermissionEventPublisher.publishPermissionDeleted(chatId, userId);

        if (removingSelf) {
            return Optional.empty();
        }

        List<ChatParticipant> updatedParticipants = chatParticipantRepository.findByIdChatId(chatId);

        List<ChatParticipantResponse> participantResponses = updatedParticipants.stream().map(chatMapper::toChatParticipantResponse).toList();

        return Optional.of(new ChatParticipantsResponse(participantResponses));
    }

    @Transactional
    public ChatParticipantResponse updateParticipant(UUID currentUserId, UUID chatId, UUID userId, UpdateChatParticipantRequest request) {
        validateUpdateParticipant(currentUserId, chatId, userId, request);

        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException(chatId));

        if (chat.getType() != ChatType.GROUP) {
            throw new InvalidChatException("Participants can be updated only in group chats");
        }

        ChatParticipant currentParticipant = chatParticipantRepository.findByIdChatIdAndIdUserId(chatId, currentUserId).orElseThrow(() -> new ChatAccessDeniedException(chatId));
        ChatParticipant targetParticipant = chatParticipantRepository.findByIdChatIdAndIdUserId(chatId, userId).orElseThrow(() -> new ParticipantNotFoundException(chatId, userId));

        if (request.getRole().isPresent()) {
            ParticipantRole newRole = request.getRole().get();
            updateParticipantRole(chatId, currentParticipant, targetParticipant, newRole);
            messagePermissionEventPublisher.publishPermissionUpserted(chatId, userId, targetParticipant.getRole());
        }

        if (request.getNickname().isPresent()) {
            String newNickname = request.getNickname().get();
            validateNickname(newNickname);
            updateParticipantNickname(chatId, currentParticipant, targetParticipant, newNickname);
        }

        ChatParticipant savedParticipant = chatParticipantRepository.save(targetParticipant);

        return chatMapper.toChatParticipantResponse(savedParticipant);
    }

    private void validateParticipantAction(UUID currentUserId, UUID chatId, UUID userId) {
        if (currentUserId == null) {
            throw new InvalidChatException("currentUserId must not be null");
        }

        if (chatId == null) {
            throw new InvalidChatException("chatId must not be null");
        }

        if (userId == null) {
            throw new InvalidChatParticipantException("userId must not be null");
        }
    }

    private void validateParticipantPermissionHierarchy(UUID chatId, ChatParticipant currentParticipant, ChatParticipant targetParticipant) {
        ParticipantRole currentRole = currentParticipant.getRole();
        ParticipantRole targetRole = targetParticipant.getRole();

        boolean changeSelf = currentParticipant.getId().getUserId().equals(targetParticipant.getId().getUserId());

        if (currentRole == ParticipantRole.OWNER) {
            return;
        }

        if (currentRole == ParticipantRole.ADMIN && (changeSelf || targetRole == ParticipantRole.MEMBER)) {
            return;
        }

        if (currentRole == ParticipantRole.MEMBER && changeSelf) {
            return;
        }

        throw new ChatAccessDeniedException(chatId);
    }

    private ChatParticipant transferOwnershipBeforeOwnerLeaves(UUID chatId, UUID currentOwnerId, UUID newOwnerId, List<ChatParticipant> participants) {
        if (newOwnerId == null) {
            throw new NewOwnerRequiredException(chatId);
        }

        if (newOwnerId.equals(currentOwnerId)) {
            throw new InvalidNewOwnerException(chatId, newOwnerId);
        }

        ChatParticipant newOwner = participants.stream()
                .filter(participant -> participant.getId().getUserId().equals(newOwnerId))
                .findFirst()
                .orElseThrow(() -> new InvalidNewOwnerException(chatId, newOwnerId));

        newOwner.changeRole(ParticipantRole.OWNER);

        return chatParticipantRepository.save(newOwner);
    }

    private void validateUpdateParticipant(UUID currentUserId, UUID chatId, UUID userId, UpdateChatParticipantRequest request) {
        if (currentUserId == null) {
            throw new InvalidChatException("currentUserId must not be null");
        }

        if (chatId == null) {
            throw new InvalidChatException("chatId must not be null");
        }

        if (userId == null) {
            throw new InvalidChatParticipantException("userId must not be null");
        }

        if (request == null) {
            throw new InvalidChatParticipantException("request must not be null");
        }

        if (!request.getRole().isPresent() && !request.getNickname().isPresent()) {
            throw new InvalidChatParticipantException("At least one field must be provided");
        }
    }

    private void validateNickname(String nickname) {
        if (nickname != null && nickname.length() > 255) {
            throw new InvalidChatParticipantException("nickname must not exceed 255 characters");
        }
    }

    private void updateParticipantRole(UUID chatId, ChatParticipant currentParticipant, ChatParticipant targetParticipant, ParticipantRole newRole) {
        if (newRole == null) {
            throw new InvalidChatParticipantException("role must not be null");
        }

        if (currentParticipant.getRole() != ParticipantRole.OWNER) {
            throw new ChatAccessDeniedException(chatId);
        }

        if (targetParticipant.getRole() == ParticipantRole.OWNER) {
            throw new InvalidChatParticipantException("Owner role cannot be changed through this endpoint");
        }

        if (newRole == ParticipantRole.OWNER) {
            throw new InvalidChatParticipantException("Owner role cannot be assigned through this endpoint");
        }

        targetParticipant.changeRole(newRole);
    }

    private void updateParticipantNickname(UUID chatId, ChatParticipant currentParticipant, ChatParticipant targetParticipant, String newNickname) {
        validateParticipantPermissionHierarchy(chatId, currentParticipant, targetParticipant);
        targetParticipant.updateNickname(newNickname);
    }
}