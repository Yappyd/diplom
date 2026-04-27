package com.yappyd.chatservice.service;

import com.yappyd.chatservice.exception.InvalidPrivateChatException;
import com.yappyd.chatservice.exception.PrivateChatWithSelfException;
import com.yappyd.chatservice.model.Chat;
import com.yappyd.chatservice.model.PrivateChat;
import com.yappyd.chatservice.repository.ChatRepository;
import com.yappyd.chatservice.repository.PrivateChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

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

                PrivateChat privateChat = new PrivateChat(
                        chat.getId(),
                        pair.userA(),
                        pair.userB()
                );
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
}