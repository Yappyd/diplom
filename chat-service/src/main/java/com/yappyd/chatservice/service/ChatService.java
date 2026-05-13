package com.yappyd.chatservice.service;

import com.yappyd.chatservice.dto.request.UpdateChatParticipantRequest;
import com.yappyd.chatservice.dto.response.ChatListResponse;
import com.yappyd.chatservice.dto.response.ChatParticipantResponse;
import com.yappyd.chatservice.dto.response.ChatParticipantsResponse;
import com.yappyd.chatservice.dto.response.ChatResponse;
import com.yappyd.chatservice.enums.ChatType;
import com.yappyd.chatservice.enums.ParticipantRole;
import com.yappyd.chatservice.exception.*;
import com.yappyd.chatservice.model.Chat;
import com.yappyd.chatservice.model.ChatParticipant;
import com.yappyd.chatservice.model.PrivateChat;
import com.yappyd.chatservice.repository.ChatParticipantRepository;
import com.yappyd.chatservice.repository.ChatRepository;
import com.yappyd.chatservice.repository.PrivateChatRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final PrivateChatRepository privateChatRepository;
    private final TransactionTemplate transactionTemplate;
    private final ChatParticipantRepository chatParticipantRepository;

    public ChatResponse createPrivateChat(UUID currentUserId, UUID targetUserId) {
        validatePrivateChat(currentUserId, targetUserId);

        UserPair pair = UserPair.of(currentUserId, targetUserId);

        PrivateChat privateChat = privateChatRepository
                .findByUserAAndUserB(pair.userA(), pair.userB())
                .orElseGet(() -> createPrivateChatWithRaceHandling(currentUserId, pair));

        Chat chat = chatRepository.findById(privateChat.getChatId())
                .orElseThrow(() -> new IllegalStateException("Chat not found for privateChatId: " + privateChat.getChatId()));

        return toChatResponse(chat, privateChat);
    }

    private void validatePrivateChat(UUID currentUserId, UUID targetUserId) {
        if (currentUserId == null) {
            throw new InvalidPrivateChatException("currentUserId must not be null");
        }
        if (targetUserId == null) {
            throw new InvalidPrivateChatException("targetUserId must not be null");
        }
        if (currentUserId.equals(targetUserId)) {
            throw new PrivateChatWithSelfException();
        }
    }

    private record UserPair(UUID userA, UUID userB) {
        private UserPair {
            if (userA == null || userB == null) {
                throw new IllegalStateException("UserPair users must not be null");
            }
            if (userA.toString().compareTo(userB.toString()) >= 0) {
                throw new IllegalStateException("userA must be less than userB");
            }
        }

        private static UserPair of(UUID user1, UUID user2) {
            if (user1.toString().compareTo(user2.toString()) < 0) {
                return new UserPair(user1, user2);
            }
            return new UserPair(user2, user1);
        }
    }

    private PrivateChat createPrivateChatWithRaceHandling(UUID currentUserId, UserPair pair) {
        try {
            UUID createdChatId = transactionTemplate.execute(status -> {
                Chat chat = Chat.createPrivateChat(currentUserId);
                chatRepository.save(chat);

                PrivateChat privateChat = new PrivateChat(chat.getId(), pair.userA(), pair.userB());
                privateChatRepository.save(privateChat);
                return chat.getId();
            });

            UUID chatId = Objects.requireNonNull(createdChatId, "createdChatId must not be null");

            return privateChatRepository.findById(chatId).orElseThrow(() -> new IllegalStateException("Created private chat not found: " + chatId));

        } catch (DataIntegrityViolationException ex) {
            return privateChatRepository.findByUserAAndUserB(pair.userA(), pair.userB()).orElseThrow(() -> ex);
        }
    }

    public ChatResponse createGroupChat(UUID currentUserId, String title, List<UUID> participantIds) {
        validateGroupChat(currentUserId, title, participantIds);

        Set<UUID> normalizedParticipantIds = normalizeGroupParticipants(
                currentUserId,
                participantIds
        );

        UUID createdChatId = transactionTemplate.execute(status -> {
            Chat chat = Chat.createGroupChat(currentUserId, title);
            chatRepository.save(chat);

            List<ChatParticipant> participants = normalizedParticipantIds.stream()
                    .map(userId -> {
                        ParticipantRole role = userId.equals(currentUserId) ? ParticipantRole.OWNER : ParticipantRole.MEMBER;
                        return new ChatParticipant(chat.getId(), userId, role);
                    }).toList();

            chatParticipantRepository.saveAll(participants);

            return chat.getId();
        });

        UUID chatId = Objects.requireNonNull(createdChatId, "createdChatId must not be null");

        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new IllegalStateException("Created group chat not found: " + chatId));

        return toChatResponse(chat, normalizedParticipantIds.stream().toList());
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

    public ChatListResponse getChats(UUID currentUserId) {
        if (currentUserId == null) {
            throw new InvalidChatException("currentUserId must not be null");
        }

        List<PrivateChat> privateChats = privateChatRepository.findByUserAOrUserB(currentUserId, currentUserId);
        List<ChatParticipant> currentUserGroupParticipants = chatParticipantRepository.findByIdUserId(currentUserId);

        List<UUID> privateChatIds = privateChats.stream().map(PrivateChat::getChatId).toList();
        List<UUID> groupChatIds = currentUserGroupParticipants.stream().map(participant -> participant.getId().getChatId()).toList();

        Set<UUID> chatIds = new LinkedHashSet<>();
        chatIds.addAll(privateChatIds);
        chatIds.addAll(groupChatIds);

        if (chatIds.isEmpty()) {
            return new ChatListResponse(List.of());
        }

        Map<UUID, PrivateChat> privateChatByChatId = privateChats.stream().collect(Collectors.toMap(PrivateChat::getChatId, Function.identity()));

        Map<UUID, List<ChatParticipant>> groupParticipantsByChatId = groupChatIds.isEmpty()
                ? Map.of() : chatParticipantRepository.findByIdChatIdIn(groupChatIds).stream()
                             .collect(Collectors.groupingBy(participant -> participant.getId().getChatId()));

        List<Chat> chats = chatRepository.findByIdInOrderByUpdatedAtDesc(chatIds);

        List<ChatResponse> chatResponses = chats.stream()
                .map(chat -> {
                    if (chat.getType() == ChatType.PRIVATE) {
                        return toChatResponse(chat, privateChatByChatId.get(chat.getId()));
                    }
                    if (chat.getType() == ChatType.GROUP) {
                        List<ChatParticipant> participants = groupParticipantsByChatId.getOrDefault(chat.getId(), List.of());
                        List<UUID> participantIds = participants.stream().map(participant -> participant.getId().getUserId()).toList();
                        return toChatResponse(chat, participantIds);
                    }
                    throw new IllegalStateException("Unsupported chat type: " + chat.getType());
                })
                .toList();

        return new ChatListResponse(chatResponses);
    }

    public ChatResponse getChat(UUID currentUserId, UUID chatId) {
        if (currentUserId == null) {
            throw new InvalidChatException("currentUserId must not be null");
        }

        if (chatId == null) {
            throw new InvalidChatException("chatId must not be null");
        }

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException(chatId));

        if (chat.getType() == ChatType.PRIVATE) {
            return getPrivateChatResponse(currentUserId, chat);
        }

        if (chat.getType() == ChatType.GROUP) {
            return getGroupChatResponse(currentUserId, chat);
        }

        throw new InvalidChatException("Unsupported chat type: " + chat.getType());
    }

    private ChatResponse getPrivateChatResponse(UUID currentUserId, Chat chat) {
        PrivateChat privateChat = privateChatRepository.findById(chat.getId())
                .orElseThrow(() -> new IllegalStateException("PrivateChat not found for chatId: " + chat.getId()));

        if (!privateChat.getUserA().equals(currentUserId) && !privateChat.getUserB().equals(currentUserId)) {
            throw new ChatAccessDeniedException(chat.getId());
        }

        return toChatResponse(chat, privateChat);
    }

    private ChatResponse getGroupChatResponse(UUID currentUserId, Chat chat) {
        List<ChatParticipant> participants = chatParticipantRepository.findByIdChatId(chat.getId());

        if (participants.isEmpty()) {
            throw new IllegalStateException("Group participants not found for chatId: " + chat.getId());
        }

        boolean currentUserIsParticipant = participants.stream().anyMatch(participant -> participant.getId().getUserId().equals(currentUserId));
        if (!currentUserIsParticipant) {
            throw new ChatAccessDeniedException(chat.getId());
        }

        List<UUID> participantIds = participants.stream().map(participant -> participant.getId().getUserId()).toList();

        return toChatResponse(chat, participantIds);
    }

    private ChatResponse toChatResponse(Chat chat, PrivateChat privateChat) {
        if (privateChat == null) {
            throw new IllegalStateException("PrivateChat not found for chatId: " + chat.getId());
        }

        return toChatResponse(chat, List.of(privateChat.getUserA(), privateChat.getUserB()));
    }

    private ChatResponse toChatResponse(Chat chat, List<UUID> participantIds) {
        return new ChatResponse(
                chat.getId(),
                chat.getType(),
                chat.getTitle(),
                chat.getCreatedBy(),
                chat.getCreatedAt(),
                chat.getUpdatedAt(),
                participantIds
        );
    }

    public ChatParticipantsResponse getChatParticipants(UUID currentUserId, UUID chatId) {
        if (currentUserId == null) {
            throw new InvalidChatException("currentUserId must not be null");
        }

        if (chatId == null) {
            throw new InvalidChatException("chatId must not be null");
        }

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException(chatId));

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

        List<ChatParticipantResponse> participantResponses = participants.stream()
                .map(this::toChatParticipantResponse)
                .toList();

        return new ChatParticipantsResponse(participantResponses);
    }

    private ChatParticipantResponse toChatParticipantResponse(ChatParticipant participant) {
        return new ChatParticipantResponse(
                participant.getId().getUserId(),
                participant.getRole(),
                participant.getNickname(),
                participant.getJoinedAt(),
                participant.getUpdatedAt()
        );
    }

    public ChatParticipantsResponse addParticipant(UUID currentUserId, UUID chatId, UUID userId) {
        validateParticipantAction(currentUserId, chatId, userId);

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException(chatId));

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

        List<ChatParticipant> updatedParticipants = chatParticipantRepository.findByIdChatId(chatId);
        List<ChatParticipantResponse> participantResponses = updatedParticipants.stream()
                .map(this::toChatParticipantResponse).toList();

        return new ChatParticipantsResponse(participantResponses);
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
                return Optional.empty();
            }

            if (ownerRemovesSelf) {
                transferOwnershipBeforeOwnerLeaves(chatId, userId, newOwnerId, participants);
            }

            chatParticipantRepository.delete(targetParticipant);

            if (removingSelf) {
                return Optional.empty();
            }

            List<ChatParticipant> updatedParticipants = chatParticipantRepository.findByIdChatId(chatId);

            List<ChatParticipantResponse> participantResponses = updatedParticipants.stream()
                    .map(this::toChatParticipantResponse)
                    .toList();

            return Optional.of(new ChatParticipantsResponse(participantResponses));
    }

    private void validateParticipantPermissionHierarchy(UUID chatId, ChatParticipant currentParticipant, ChatParticipant targetParticipant) {
        ParticipantRole currentRole = currentParticipant.getRole();
        ParticipantRole targetRole = targetParticipant.getRole();

        boolean changeSelf = currentParticipant.getId().getUserId().equals(targetParticipant.getId().getUserId());

        if (currentRole == ParticipantRole.OWNER) return;
        if (currentRole == ParticipantRole.ADMIN && (changeSelf || targetRole == ParticipantRole.MEMBER)) return;
        if (currentRole == ParticipantRole.MEMBER && changeSelf) return;

        throw new ChatAccessDeniedException(chatId);
    }

    private void transferOwnershipBeforeOwnerLeaves(UUID chatId, UUID currentOwnerId, UUID newOwnerId, List<ChatParticipant> participants) {
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
        chatParticipantRepository.save(newOwner);
    }

    @Transactional
    public ChatParticipantResponse updateParticipant(UUID currentUserId, UUID chatId, UUID userId, UpdateChatParticipantRequest request) {
        validateUpdateParticipant(currentUserId, chatId, userId, request);

        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException(chatId));
        if (chat.getType() != ChatType.GROUP) {
            throw new InvalidChatException("Participants can be updated only in group chats");
        }

        ChatParticipant currentParticipant = chatParticipantRepository.findByIdChatIdAndIdUserId(chatId, currentUserId)
                .orElseThrow(() -> new ChatAccessDeniedException(chatId));
        ChatParticipant targetParticipant = chatParticipantRepository.findByIdChatIdAndIdUserId(chatId, userId)
                .orElseThrow(() -> new ParticipantNotFoundException(chatId, userId));

        if (request.getRole().isPresent()) {
            ParticipantRole newRole = request.getRole().get();
            updateParticipantRole(chatId, currentParticipant, targetParticipant, newRole);
        }

        if (request.getNickname().isPresent()) {
            String newNickname = request.getNickname().get();
            validateNickname(newNickname);
            updateParticipantNickname(chatId, currentParticipant, targetParticipant, newNickname);
        }

        ChatParticipant savedParticipant = chatParticipantRepository.save(targetParticipant);

        return toChatParticipantResponse(savedParticipant);
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