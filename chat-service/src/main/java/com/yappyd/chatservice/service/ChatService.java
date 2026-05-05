package com.yappyd.chatservice.service;

import com.yappyd.chatservice.dto.response.ChatListResponse;
import com.yappyd.chatservice.dto.response.ChatResponse;
import com.yappyd.chatservice.enums.ChatType;
import com.yappyd.chatservice.exception.*;
import com.yappyd.chatservice.model.Chat;
import com.yappyd.chatservice.model.PrivateChat;
import com.yappyd.chatservice.repository.ChatRepository;
import com.yappyd.chatservice.repository.PrivateChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final PrivateChatRepository privateChatRepository;
    private final TransactionTemplate transactionTemplate;


    public UUID createPrivateChat(UUID currentUserId, UUID targetUserId) {
        validatePrivateChat(currentUserId, targetUserId);

        UserPair pair = UserPair.of(currentUserId, targetUserId);

        return privateChatRepository
                .findByUserAAndUserB(pair.userA(), pair.userB())
                .map(PrivateChat::getChatId)
                .orElseGet(() -> createPrivateChatWithRaceHandling(currentUserId, pair));
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

    private UUID createPrivateChatWithRaceHandling(UUID currentUserId, UserPair pair) {
        try {
            return transactionTemplate.execute(status -> {
                Chat chat = Chat.createPrivateChat(currentUserId);
                chatRepository.save(chat);

                PrivateChat privateChat = new PrivateChat(chat.getId(), pair.userA(), pair.userB());
                privateChatRepository.save(privateChat);

                return chat.getId();
            });
        } catch (DataIntegrityViolationException ex) {
            return privateChatRepository
                    .findByUserAAndUserB(pair.userA(), pair.userB())
                    .map(PrivateChat::getChatId)
                    .orElseThrow(() -> ex);
        }
    }

    public ChatListResponse getChats(UUID currentUserId) {
        if (currentUserId == null) {
            throw new InvalidChatException("currentUserId must not be null");
        }

        List<PrivateChat> privateChats = privateChatRepository.findByUserAOrUserB(currentUserId, currentUserId);

        if (privateChats.isEmpty()) {
            return new ChatListResponse(List.of());
        }

        Map<UUID, PrivateChat> privateChatByChatId = privateChats.stream()
                .collect(Collectors.toMap(
                        PrivateChat::getChatId,
                        Function.identity()
                ));

        List<UUID> chatIds = privateChats.stream()
                .map(PrivateChat::getChatId)
                .toList();

        List<Chat> chats = chatRepository.findByIdInOrderByUpdatedAtDesc(chatIds);

        List<ChatResponse> chatResponses = chats.stream()
                .map(chat -> toChatResponse(chat, privateChatByChatId.get(chat.getId())))
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

        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException(chatId));

        if (chat.getType() == ChatType.PRIVATE) {
            return getPrivateChatResponse(currentUserId, chat);
        }

        throw new InvalidChatException("Unsupported chat type: " + chat.getType());
    }

    private ChatResponse toChatResponse(Chat chat, PrivateChat privateChat) {
        if (privateChat == null) {
            throw new IllegalStateException("PrivateChat not found for chatId: " + chat.getId());
        }

        return new ChatResponse(
                chat.getId(),
                chat.getType(),
                chat.getTitle(),
                chat.getCreatedBy(),
                chat.getCreatedAt(),
                chat.getUpdatedAt(),
                List. of(
                        privateChat.getUserA(),
                        privateChat.getUserB()
                )
        );
    }

    private ChatResponse getPrivateChatResponse(UUID currentUserId, Chat chat) {
        PrivateChat privateChat = privateChatRepository.findById(chat.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "PrivateChat not found for chatId: " + chat.getId()
                ));

        if (!privateChat.getUserA().equals(currentUserId) && !privateChat.getUserB().equals(currentUserId)) {
            throw new ChatAccessDeniedException(chat.getId());
        }

        return toChatResponse(chat, privateChat);
    }
}