package com.yappyd.chatservice.service;

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
}